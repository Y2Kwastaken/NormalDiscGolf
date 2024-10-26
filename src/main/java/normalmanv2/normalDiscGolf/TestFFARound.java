package normalmanv2.normalDiscGolf;

import normalmanv2.normalDiscGolf.api.NDGApi;
import normalmanv2.normalDiscGolf.impl.course.Course;
import normalmanv2.normalDiscGolf.impl.round.CourseDifficulty;
import normalmanv2.normalDiscGolf.impl.round.FFARound;
import normalmanv2.normalDiscGolf.impl.round.GameRound;
import normalmanv2.normalDiscGolf.impl.round.RoundHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestFFARound implements CommandExecutor {

    private final NormalDiscGolf plugin;
    private final RoundHandler roundHandler;

    public TestFFARound(NDGApi ndgApi, NormalDiscGolf plugin) {
        this.roundHandler = ndgApi.getRoundHandler();
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        final GameRound round;

        if (!(commandSender instanceof Player player)) {
            return false;
        }

        if (args.length != 1) {
            return false;
        }

        if (args[0].equalsIgnoreCase("start")) {
            round = new FFARound(plugin, new Course(CourseDifficulty.EASY, "Test Course", 18, new Location(player.getWorld(), 19, 68, -107)));
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                round.addPlayer(player1.getUniqueId());
            }
            if (roundHandler.startRound(round)) {
                System.out.println("Error starting round");
            }

        } else if (args[0].equalsIgnoreCase("end")) {
            for (GameRound round1 : roundHandler.getActiveRounds()) {
                if (round1.getTask() == null) {
                    continue;
                }
                roundHandler.endRound(round1);
            }
        }

        return true;
    }
}
