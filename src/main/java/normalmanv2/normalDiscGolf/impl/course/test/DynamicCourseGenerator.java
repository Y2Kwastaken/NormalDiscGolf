package normalmanv2.normalDiscGolf.impl.course.test;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class DynamicCourseGenerator {

    private static final double FAIRWAY_RATIO = 0.30;
    private static final double OBSTACLE_RATIO = 0.20;
    private static final double WATER_RATIO = 0.05;
    private static final int WATER_CLUSTER_MIN = 3;
    private static final int WATER_CLUSTER_MAX = 4;

    private final int numHoles;
    private final List<Hole> holes = new ArrayList<>();
    private final Random random = new Random();
    private final List<Coordinate> globalFairways = new ArrayList<>();
    private Terrain[][] terrainGrid;

    private final Location superPosition;

    public DynamicCourseGenerator(int numHoles, World world) {
        this.numHoles = numHoles;
        this.superPosition = world.getSpawnLocation();
    }

    public CourseTest generateCourse() {
        int gridSize = this.calculateDynamicGridSize(this.numHoles);
        this.terrainGrid = new Terrain[gridSize][gridSize];
        this.initializeTerrain(this.terrainGrid);

        for (int i = 0; i < this.numHoles; i++) {
            Hole hole = this.generateHole(i, this.terrainGrid);
            this.holes.add(hole);
            this.updateTerrainForHole(hole, this.terrainGrid);
        }

        this.addObstaclesAndWater(this.terrainGrid, this.globalFairways);
        return new CourseTest(this.holes, this.terrainGrid);
    }

    private int calculateDynamicGridSize(int numHoles) {
        return (int) Math.sqrt(numHoles) * 5;
    }

    private void initializeTerrain(Terrain[][] terrainGrid) {
        for (Terrain[] terrains : terrainGrid) {
            Arrays.fill(terrains, Terrain.NATURAL);
        }
    }

    private Hole generateHole(int holeIndex, Terrain[][] terrainGrid) {
        Coordinate tee = placeTee(terrainGrid);
        Coordinate pin = placePin(terrainGrid, tee);
        List<Coordinate> fairway = generateFairway(tee, pin, terrainGrid);

        this.globalFairways.addAll(fairway);
        int par = calculatePar(tee, pin, fairway, terrainGrid);
        return new Hole(holeIndex + 1, tee, pin, fairway, par);
    }

    private Coordinate placeTee(Terrain[][] terrainGrid) {
        int x, z;
        do {
            x = this.random.nextInt(terrainGrid.length);
            z = this.random.nextInt(terrainGrid[0].length);
        } while (terrainGrid[x][z] != Terrain.NATURAL);

        return new Coordinate(x, z);
    }

    private Coordinate placePin(Terrain[][] terrainGrid, Coordinate tee) {
        Coordinate pin;
        do {
            int distance = random.nextInt(8) + 12;
            double angle = random.nextDouble() * 2 * Math.PI;
            int xOffset = (int) (distance * Math.cos(angle));
            int zOffset = (int) (distance * Math.sin(angle));

            int x = Math.max(0, Math.min(terrainGrid.length - 1, tee.x() + xOffset));
            int z = Math.max(0, Math.min(terrainGrid[0].length - 1, tee.z() + zOffset));
            pin = new Coordinate(x, z);
        } while (terrainGrid[pin.x()][pin.z()] != Terrain.NATURAL || pin.equals(tee));

        return pin;
    }

    private List<Coordinate> generateFairway(Coordinate tee, Coordinate pin, Terrain[][] terrainGrid) {
        List<Coordinate> fairway = new ArrayList<>();
        int x = tee.x(), z = tee.z();
        fairway.add(new Coordinate(x, z));

        while (x != pin.x() || z != pin.z()) {
            if (random.nextBoolean()) {
                if (x < pin.x()) x++;
                else if (x > pin.x()) x--;
            } else {
                if (z < pin.z()) z++;
                else if (z > pin.z()) z--;
            }

            int widthChance = random.nextInt(3);
            for (int i = -widthChance; i <= widthChance; i++) {
                for (int j = -widthChance; j <= widthChance; j++) {
                    int nx = Math.max(0, Math.min(terrainGrid.length - 1, x + i));
                    int nz = Math.max(0, Math.min(terrainGrid[0].length - 1, z + j));
                    if (terrainGrid[nx][nz] == Terrain.NATURAL) {
                        terrainGrid[nx][nz] = Terrain.FAIRWAY;
                        fairway.add(new Coordinate(nx, nz));
                    }
                }
            }

            Coordinate step = new Coordinate(x, z);
            if (!fairway.contains(step)) {
                fairway.add(step);
            }
        }

        return fairway;
    }

    private int calculatePar(Coordinate tee, Coordinate pin, List<Coordinate> fairway, Terrain[][] terrainGrid) {
        int distance = (int) Math.sqrt(Math.pow(pin.x() - tee.x(), 2) + Math.pow(pin.z() - tee.z(), 2));
        int basePar;

        if (distance <= 75) {
            basePar = 3;
        } else if (distance <= 150) {
            basePar = 4;
        } else {
            basePar = 5;
        }

        int finalPar = this.getFinalPar(fairway, terrainGrid, basePar);

        return Math.max(3, finalPar);
    }

    private int getFinalPar(List<Coordinate> fairway, Terrain[][] terrainGrid, int basePar) {
        int waterCount = 0;
        int obstacleCount = 0;
        for (Coordinate coordinate : fairway) {
            Terrain terrain = terrainGrid[coordinate.x()][coordinate.z()];
            if (terrain == Terrain.WATER) {
                waterCount++;
            } else if (terrain == Terrain.OBSTACLE) {
                obstacleCount++;
            }
        }

        int waterModifier = waterCount / 2;

        int obstacleModifier = obstacleCount / 3;

        return basePar + waterModifier + obstacleModifier;
    }

    private void updateTerrainForHole(Hole hole, Terrain[][] terrainGrid) {
        terrainGrid[hole.tee().x()][hole.tee().z()] = Terrain.TEE;
        terrainGrid[hole.pin().x()][hole.pin().z()] = Terrain.PIN;

        for (Coordinate coordinate : hole.fairway()) {
            if (terrainGrid[coordinate.x()][coordinate.z()] == Terrain.TEE || terrainGrid[coordinate.x()][coordinate.z()] == Terrain.PIN) {
                return;
            }
            terrainGrid[coordinate.x()][coordinate.z()] = Terrain.FAIRWAY;
        }
    }

    private void addObstaclesAndWater(Terrain[][] terrainGrid, List<Coordinate> fairways) {
        int totalTiles = terrainGrid.length * terrainGrid[0].length;
        int obstacleCount = (int) (totalTiles * OBSTACLE_RATIO);
        int waterCount = (int) (totalTiles * WATER_RATIO);

        placeObstacles(terrainGrid, obstacleCount, Terrain.OBSTACLE, fairways);
        placeClusters(terrainGrid, waterCount, WATER_CLUSTER_MIN, WATER_CLUSTER_MAX, Terrain.WATER);
    }

    private void placeObstacles(Terrain[][] terrainGrid, int count, Terrain terrain, List<Coordinate> fairways) {
        for (int i = 0; i < count / 2; i++) {
            Coordinate nearFairway = getNearbyFairway(fairways, terrainGrid, terrain);
            if (nearFairway != null) {
                terrainGrid[nearFairway.x()][nearFairway.z()] = terrain;
            }
        }
        for (int i = 0; i < count / 2; i++) {
            int x, z;
            do {
                x = random.nextInt(terrainGrid.length);
                z = random.nextInt(terrainGrid[0].length);
            } while (terrainGrid[x][z] != Terrain.NATURAL);

            terrainGrid[x][z] = terrain;
        }
    }

    private Coordinate getNearbyFairway(List<Coordinate> fairways, Terrain[][] terrainGrid, Terrain terrain) {
        if (fairways.isEmpty()) return null;

        Coordinate fairwayPoint = fairways.get(random.nextInt(fairways.size()));
        int offset = random.nextInt(2) + 2;
        for (int i = 0; i < 10; i++) {
            int dx = random.nextBoolean() ? offset : -offset;
            int dz = random.nextBoolean() ? offset : -offset;
            int nx = Math.max(0, Math.min(terrainGrid.length - 1, fairwayPoint.x() + dx));
            int nz = Math.max(0, Math.min(terrainGrid[0].length - 1, fairwayPoint.z() + dz));
            if (terrainGrid[nx][nz] == Terrain.NATURAL) {
                return new Coordinate(nx, nz);
            }
        }
        return null;
    }

    private void placeClusters(Terrain[][] terrainGrid, int count, int minSize, int maxSize, Terrain terrain) {
        while (count > 0) {
            int clusterSize = Math.min(random.nextInt(maxSize - minSize + 1) + minSize, count);

            int x, z;
            do {
                x = random.nextInt(terrainGrid.length);
                z = random.nextInt(terrainGrid[0].length);
            } while (!isValidClusterStart(terrainGrid, x, z));

            for (int i = 0; i < clusterSize; i++) {
                int direction = random.nextInt(4);
                int nx = Math.max(0, Math.min(terrainGrid.length - 1, x + (direction == 0 ? 1 : direction == 1 ? -1 : 0)));
                int nz = Math.max(0, Math.min(terrainGrid[0].length - 1, z + (direction == 2 ? 1 : direction == 3 ? -1 : 0)));

                if (terrainGrid[nx][nz] == Terrain.NATURAL) {
                    terrainGrid[nx][nz] = terrain;
                    count--;
                }
            }
        }
    }

    private boolean isValidClusterStart(Terrain[][] terrainGrid, int x, int z) {
        return terrainGrid[x][z] == Terrain.NATURAL && !isNearSpecialTerrain(x, z, terrainGrid);
    }

    private boolean isNearSpecialTerrain(int x, int z, Terrain[][] terrainGrid) {
        int radius = 1;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int nx = Math.max(0, Math.min(terrainGrid.length - 1, x + dx));
                int nz = Math.max(0, Math.min(terrainGrid[0].length - 1, z + dz));
                if (terrainGrid[nx][nz] == Terrain.TEE || terrainGrid[nx][nz] == Terrain.PIN) {
                    return true;
                }
            }
        }
        return false;
    }

    public void printGrid() {
        if (this.terrainGrid == null) {
            System.out.println("The course has not been generated yet!");
            return;
        }

        for (Terrain[] row : this.terrainGrid) {
            for (Terrain terrain : row) {
                if (terrain == null) throw new RuntimeException("Terrain is null!");
                System.out.print(switch (terrain) {
                    case NATURAL -> "N";
                    case FAIRWAY -> "F";
                    case TEE -> "T";
                    case PIN -> "P";
                    case OBSTACLE -> "O";
                    case WATER -> "W";
                });
            }
            System.out.println();
        }
    }

    private final Map<Terrain, Material> terrainToBlocks = Map.of(
            Terrain.NATURAL, Material.GRASS_BLOCK,
            Terrain.FAIRWAY, Material.GREEN_STAINED_GLASS,
            Terrain.OBSTACLE, Material.OAK_WOOD,
            Terrain.WATER, Material.WATER,
            Terrain.TEE, Material.STONE,
            Terrain.PIN, Material.GOLD_BLOCK
    );


    public void createTileMesh(World world) {
        boolean[][] visited = new boolean[terrainGrid.length][terrainGrid[0].length];
        int tileSize = 8;

        for (int x = 0; x < terrainGrid.length; x++) {
            for (int z = 0; z < terrainGrid[0].length; z++) {
                if (!visited[x][z]) {
                    Terrain terrain = terrainGrid[x][z];
                    List<Coordinate> region = new ArrayList<>();
                    floodFill(x, z, terrain, visited, region);

                    createRegion(world, region, terrain, tileSize);
                }
            }
        }
    }

    private void floodFill(int x, int z, Terrain terrain, boolean[][] visited, List<Coordinate> region) {
        if (x < 0 || z < 0 || x >= terrainGrid.length || z >= terrainGrid[0].length) return;
        if (visited[x][z] || terrainGrid[x][z] != terrain) return;

        visited[x][z] = true;
        region.add(new Coordinate(x, z));

        floodFill(x + 1, z, terrain, visited, region);
        floodFill(x - 1, z, terrain, visited, region);
        floodFill(x, z + 1, terrain, visited, region);
        floodFill(x, z - 1, terrain, visited, region);
    }

    private void createRegion(World world, List<Coordinate> region, Terrain terrain, int tileSize) {
        Material material = terrainToBlocks.get(terrain);
        for (Coordinate coordinate : region) {
            int baseX = superPosition.getBlockX() + coordinate.x() * tileSize;
            int baseZ = superPosition.getBlockZ() + coordinate.z() * tileSize;

            for (int bx = 0; bx < tileSize; bx++) {
                for (int bz = 0; bz < tileSize; bz++) {
                    int worldX = baseX + bx;
                    int worldZ = baseZ + bz;
                    int worldY = 64;
                    world.getBlockAt(worldX, worldY, worldZ).setType(material);
                }
            }
        }
    }

}
