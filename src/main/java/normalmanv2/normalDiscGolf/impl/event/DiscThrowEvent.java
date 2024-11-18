package normalmanv2.normalDiscGolf.impl.event;

import normalmanv2.normalDiscGolf.impl.course.CourseImpl;
import normalmanv2.normalDiscGolf.common.disc.DiscImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DiscThrowEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final DiscImpl thrownDiscImpl;
    private final CourseImpl currentCourseImpl;
    private final int holeNumber;
    private final Player player;

    public DiscThrowEvent(DiscImpl thrownDiscImpl, CourseImpl currentCourseImpl, int holeNumber, Player player) {
        this.thrownDiscImpl = thrownDiscImpl;
        this.currentCourseImpl = currentCourseImpl;
        this.holeNumber = holeNumber;
        this.player = player;
    }

    public DiscImpl getThrownDisc() {
        return this.thrownDiscImpl;
    }

    public CourseImpl getCurrentCourse() {
        return this.currentCourseImpl;
    }

    public int getHoleNumber() {
        return this.holeNumber;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
