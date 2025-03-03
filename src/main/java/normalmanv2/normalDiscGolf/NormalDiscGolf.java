package normalmanv2.normalDiscGolf;

import normalmanv2.normalDiscGolf.impl.NDGManager;
import normalmanv2.normalDiscGolf.impl.listener.DiscThrowListener;
import normalmanv2.normalDiscGolf.impl.listener.GoalScoreListener;
import normalmanv2.normalDiscGolf.impl.listener.GuiListener;
import normalmanv2.normalDiscGolf.impl.listener.PlayerJoinListener;
import normalmanv2.normalDiscGolf.impl.listener.PlayerRoundQueueListener;
import normalmanv2.normalDiscGolf.impl.resourcepack.PackedIntegration;
import normalmanv2.normalDiscGolf.test.BackHandTest;
import normalmanv2.normalDiscGolf.test.DynamicCourseGeneratorTest;
import normalmanv2.normalDiscGolf.test.ForehandTest;
import normalmanv2.normalDiscGolf.test.RoundQueueTest;
import normalmanv2.normalDiscGolf.test.TestFFARound;
import normalmanv2.normalDiscGolf.test.WFCTest;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class NormalDiscGolf extends JavaPlugin {

    @Override
    public void onEnable() {
        this.registerCommands();
        this.registerListeners();
    /*
        try {
            PackedIntegration integration = new PackedIntegration(this);
            integration.savePack();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        */
    }

    private void registerCommands() {
        getCommand("testBackHand").setExecutor(new BackHandTest(NDGManager.getInstance()));
        getCommand("testForeHand").setExecutor(new ForehandTest(NDGManager.getInstance()));
        getCommand("startRound").setExecutor(new TestFFARound(NDGManager.getInstance(), this));
        getCommand("wfctest").setExecutor(new WFCTest(this));
        getCommand("roundQueueTest").setExecutor(new RoundQueueTest(NDGManager.getInstance().getGuiManager()));


        this.registerAbstractCommands();
    }

    private void registerAbstractCommands() {
        System.out.println("Registering abstract commands!");
        new DynamicCourseGeneratorTest(this);
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new GoalScoreListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRoundQueueListener(), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(NDGManager.getInstance().getGuiManager()), this);
        Bukkit.getPluginManager().registerEvents(new DiscThrowListener(), this);
    }

    @Override
    public void onDisable() {
    }



}
