package normalmanv2.normalDiscGolf;

import normalmanv2.normalDiscGolf.impl.player.score.PlayerScoreCard;
import normalmanv2.normalDiscGolf.impl.round.FFARound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class GoalScoreListener implements Listener {

    @EventHandler
    public void onGoalScore(GoalScoreEvent event) {
        handleScoring((FFARound) event.getRound(), event.getPlayer().getUniqueId());
        System.out.println("GoalScoreEventListener Invoked");
    }

    private void handleScoring(FFARound round, UUID playerId) {
        PlayerScoreCard scoreCard = round.getScoreCards().get(playerId);
        scoreCard.recordHoleScore(1, scoreCard.getCurrentStrokes(), 3);
        scoreCard.resetCurrentStrokes();
    }

}
