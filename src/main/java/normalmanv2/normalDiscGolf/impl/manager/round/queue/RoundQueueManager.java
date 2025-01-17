package normalmanv2.normalDiscGolf.impl.manager.round.queue;

import normalmanv2.normalDiscGolf.api.round.GameRound;
import normalmanv2.normalDiscGolf.api.team.Team;
import normalmanv2.normalDiscGolf.impl.NDGManager;
import normalmanv2.normalDiscGolf.api.division.Division;
import normalmanv2.normalDiscGolf.impl.manager.round.lifecycle.RoundHandler;
import normalmanv2.normalDiscGolf.impl.player.PlayerData;
import normalmanv2.normalDiscGolf.impl.player.PlayerDataManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RoundQueueManager {

    private final RoundHandler roundHandler;
    private final Set<GameRound> queuedRounds;
    private final Map<Division, Map<UUID, String>> playerQueue;
    private final PlayerDataManager playerDataManager;

    public RoundQueueManager(RoundHandler roundHandler, PlayerDataManager playerDataManager) {
        this.roundHandler = roundHandler;
        this.playerDataManager = playerDataManager;
        this.queuedRounds = new HashSet<>();
        this.playerQueue = new HashMap<>();
        for (Division division : Division.values()) {
            this.playerQueue.put(division, new HashMap<>());
        }
    }

    public void addPlayerToQueue(UUID player, String roundType) {
        PlayerData playerData = NDGManager.getInstance().getPlayerDataManager().getDataByPlayer(player);
        Division division = playerData.getRating().getDivision();

        this.playerQueue.get(division).put(player, roundType);
    }

    public void removePlayerFromQueue(UUID player) {
        PlayerData playerData = playerDataManager.getDataByPlayer(player);
        Division division = playerData.getRating().getDivision();
        this.playerQueue.get(division).remove(player);
    }

    public boolean isPlayerQueued(UUID player) {
        PlayerData playerData = playerDataManager.getDataByPlayer(player);
        Division division = playerData.getRating().getDivision();
        return this.playerQueue.get(division).containsKey(player);
    }

    public void addRoundToQueue(GameRound round) {
        this.queuedRounds.add(round);
    }

    public void removeRoundFromQueue(GameRound round) {
        this.queuedRounds.remove(round);
    }

    public boolean isRoundQueued(GameRound round) {
        return this.queuedRounds.contains(round);
    }

    public Map<UUID, String> getPlayerQueue(Division division) {
        return this.playerQueue.containsKey(division)
                ? Collections.unmodifiableMap(this.playerQueue.get(division))
                : Collections.emptyMap();
    }

    public Set<GameRound> getQueuedRounds() {
        return Collections.unmodifiableSet(this.queuedRounds);
    }

    public void tickPlayerQueue() {
        for (Division division : Division.values()) {
            Map<UUID, String> divisionQueue = this.playerQueue.get(division);
            if (divisionQueue.keySet().isEmpty()) continue;

            Iterator<UUID> iterator = divisionQueue.keySet().iterator();

            while (iterator.hasNext()) {
                UUID player = iterator.next();
                String roundType = divisionQueue.get(player);
                GameRound round = findOrCreateRoundForDivision(division, roundType, false);

                if (round == null)
                    throw new RuntimeException("Error ticking player queue: No suitable round could be found!");
                if (this.roundHandler.getActiveRounds().contains(round)) continue;

                for (Team team : round.getTeams()) {
                    if (team.isFull()) continue;

                    team.addPlayer(player);
                    iterator.remove();
                }
            }
            divisionQueue.clear();
        }
    }

    public void tickRoundQueue() {
        for (GameRound round : this.queuedRounds) {
            if (round.getTeams().stream().allMatch(Team::isFull) && round.isFull()) {
                round.startRound();
                this.queuedRounds.remove(round);
            }
        }
    }

    private GameRound findOrCreateRoundForDivision(Division division, String roundType, boolean isTournamentRound) {
        return this.queuedRounds.stream()
                .filter(round -> round.getDivision() == division && round.getId().equalsIgnoreCase(roundType) && round.isTournamentRound() == isTournamentRound)
                .findFirst()
                .orElseGet(() -> {
                    GameRound newRound = roundHandler.createRound(division, roundType, isTournamentRound);
                    this.addRoundToQueue(newRound);
                    return newRound;
                });
    }
}
