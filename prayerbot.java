package bot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.List;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.queries.SimpleItemQuery;
import simple.hooks.queries.SimplePlayerQuery;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimplePlayer;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

@ScriptManifest(author = "REDRUM", category = Category.PRAYER, description = "Start in magebank or edgeville, have your first preset set with a full inventory of the bones you wish to bury and start the bot.", discord = "REDRUM#9269", name = "Mage bank burial", servers = {"Novea", "novea"}, version = "0.1")
public class Main extends Script implements LoopingScript {
  private long STARTTIME;
  
  private long UPTIME;
  
  public int startPrayerXp;
  
  public int prayerXpGained;
  
  public int CURRENT_PRAYER_XP;
  
  public int prayerlvl;
  
  public int bonesBurried;
  
  public int levelsUp;
  
  public int invDone;
  
  private WorldArea edge = new WorldArea(new WorldPoint(3098, 3499, 0), new WorldPoint(3091, 3488, 0));
  
  private WorldArea mage_bank = new WorldArea(new WorldPoint(2546, 4711, 0), new WorldPoint(2531, 4723, 0));
  
  private List<String> staff_names = Arrays.asList(new String[] { 
        "Chris T", "Chas", "Fallen Angel", "Chamon", "Fe Bulk", "NotAcatfish", "Hiddenkid", "NightHawk", "I don't bot", "Mole", 
        "Slepz", "Elite Iron", "Mcchouffe", "RyanSG93", "Auqment" });
  
  private String status = "";
  
  public void paint(Graphics Graphs) {
    this.CURRENT_PRAYER_XP = this.ctx.skills.experience(SimpleSkills.Skills.PRAYER);
    this.UPTIME = System.currentTimeMillis() - this.STARTTIME;
    Graphics2D g = (Graphics2D)Graphs;
    Stroke stroke3 = new BasicStroke(2.0F, 2, 1);
    Font smallFont = new Font("Courier", 1, 12);
    Font bigFont = new Font("Courier", 1, 18);
    g.setStroke(stroke3);
    g.setColor(new Color(158, 66, 245, 125));
    g.fillRect(7, 345, 505, 130);
    g.setColor(new Color(177, 110, 245, 125));
    g.drawRect(7, 345, 505, 130);
    g.setStroke(new BasicStroke(1.0F));
    g.setColor(Color.white);
    g.setFont(bigFont);
    g.drawString("Mage bank burial", 12, 360);
    g.setFont(smallFont);
    g.drawString("Total XP: " + runescapeFormat(Integer.valueOf(this.CURRENT_PRAYER_XP - this.startPrayerXp)), 12, 395);
    g.drawString("Potential XP/h :" + runescapeFormat(Integer.valueOf(this.ctx.paint.valuePerHour(this.CURRENT_PRAYER_XP - this.startPrayerXp, this.STARTTIME))), 190, 395);
    g.drawString("Inventories: " + this.invDone, 190, 430);
    g.drawString("Bones Burried: " + this.bonesBurried, 12, 430);
    g.drawString("Levels Gained: " + this.levelsUp, 395, 395);
    g.drawString("Uptime: " + this.ctx.paint.formatTime(this.UPTIME), 395, 430);
    g.drawString("Status: " + this.status, 12, 470);
  }
  
  public void onChatMessage(ChatMessage msg) {
    if (msg.getMessage().contains("dig a hole"))
      this.bonesBurried++; 
    if (msg.getMessage().contains("just advanced a"))
      this.levelsUp++; 
  }
  
  public void onExecute() {
    this.CURRENT_PRAYER_XP = this.ctx.skills.experience(SimpleSkills.Skills.PRAYER);
    this.startPrayerXp = this.ctx.skills.experience(SimpleSkills.Skills.PRAYER);
    this.STARTTIME = System.currentTimeMillis();
    this.prayerlvl = Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.PRAYER)).intValue();
    setZoom();
  }
  
  public void onProcess() {
    this.status = "Starting";
    if (this.edge.containsPoint(this.ctx.players.getLocal().getLocation()) && !hasBones()) {
      this.status = "Teleporting";
      teleportToMb();
    } 
    if (isInMageBank() && !hasBones())
      PresetSupplies(); 
    if (isInMageBank() && hasBones()) {
      this.status = "walking to chest";
      if (this.ctx.pathing.onTile(new WorldPoint(2534, 4712, 0))) {
        if (hasBones()) {
          this.status = "burrying";
          if (((SimpleItemQuery)this.ctx.inventory.populate()).population() > 0) {
            SimpleItemQuery<SimpleItem> items = ((SimpleItemQuery)((SimpleItemQuery)this.ctx.inventory.populate()).filter(o -> o.getName().contains("bones"))).filterHasAction(new String[] { "Bury" });
            for (SimpleItem item : items) {
              item.click(0);
              this.ctx.sleep(600);
            } 
            SimpleItemQuery<SimpleItem> itemes = ((SimpleItemQuery)((SimpleItemQuery)this.ctx.inventory.populate()).filter(o -> o.getName().contains("Bones"))).filterHasAction(new String[] { "Bury" });
            for (SimpleItem itemd : itemes) {
              itemd.click(0);
              this.ctx.sleep(600);
            } 
          } 
        } else {
          this.status = "Inventory complete.";
          PresetSupplies();
        } 
      } else {
        setZoom();
        WalktoChest();
        this.ctx.sleep(500);
      } 
    } 
    if (staffFound()) {
      teleportToMb();
      this.status = "Staff member found, sleeping 10 mins";
      this.ctx.sleep(600000);
    } 
  }
  
  public void openTab(Game.Tab tab) {
    if (!isTabOpen(tab))
      (ClientContext.instance()).game.tab(tab); 
  }
  
  public void setZoom() {
    ClientContext ctx = ClientContext.instance();
    ctx.viewport.pitch(100);
    ctx.viewport.angle(0);
    openTab(Game.Tab.OPTIONS);
    SimpleWidget widget = ctx.widgets.getWidget(259, 9);
    if (widget != null && widget.visibleOnScreen())
      widget.click(0); 
    openInvTab();
  }
  
  private void teleportToMb() {
    this.ctx.keyboard.sendKeys("::mb");
    this.ctx.sleep(1000);
  }
  
  private void WalktoChest() {
    this.ctx.pathing.clickSceneTile(new WorldPoint(2534, 4712, 0), false, false);
    this.ctx.sleep(1000);
  }
  
  private boolean isInMageBank() {
    return this.mage_bank.containsPoint(this.ctx.players.getLocal().getLocation());
  }
  
  private void PresetSupplies() {
    if (isInMageBank()) {
      this.status = "Stocking Bones";
      openQuestTab();
      SimpleWidget widget = this.ctx.widgets.getWidget(259, 9);
      if (widget != null && widget.visibleOnScreen())
        widget.click(1); 
      this.ctx.sleep(300);
      SimpleWidget widget2 = this.ctx.widgets.getWidget(259, 4).getChild(1);
      if (widget2 != null && widget2.visibleOnScreen())
        widget2.click(0); 
      this.ctx.sleep(300);
      this.ctx.dialogue.clickDialogueOption(1);
      this.ctx.sleep(300);
      this.invDone++;
      openInvTab();
    } 
  }
  
  private boolean isTabOpen(Game.Tab tab) {
    return this.ctx.game.tab().equals(tab);
  }
  
  private void openQuestTab() {
    if (!isTabOpen(Game.Tab.QUESTS) && 
      this.ctx.game.tab(Game.Tab.QUESTS))
      this.ctx.sleep(300); 
  }
  
  private void openInvTab() {
    if (!isTabOpen(Game.Tab.INVENTORY) && 
      this.ctx.game.tab(Game.Tab.INVENTORY))
      this.ctx.sleep(300); 
  }
  
  private boolean hasBones() {
    if (containsItem("Dragon bones"))
      return true; 
    if (containsItem("Bones"))
      return true; 
    return false;
  }
  
  private boolean containsItem(String itemName) {
    return !((SimpleItemQuery)((SimpleItemQuery)this.ctx.inventory.populate()).filter(p -> p.getName().contains(paramString))).isEmpty();
  }
  
  private boolean staffFound() {
    SimplePlayerQuery<SimplePlayer> players = (SimplePlayerQuery<SimplePlayer>)this.ctx.players.populate();
    for (SimplePlayer p : players) {
      boolean isMatched = this.staff_names.stream().anyMatch(p.getName()::equalsIgnoreCase);
      if (isMatched && this.ctx.pathing.inArea(this.mage_bank))
        return true; 
    } 
    return false;
  }
  
  public void onTerminate() {}
  
  public static String runescapeFormat(Integer number) {
    String[] suffix = { "K", "M", "B", "T" };
    int size = (number.intValue() != 0) ? (int)Math.log10(number.intValue()) : 0;
    if (size >= 3)
      while (size % 3 != 0)
        size--;  
    return (size >= 3) ? (String.valueOf(String.valueOf(Math.round(number.intValue() / Math.pow(10.0D, size) * 10.0D) / 10.0D)) + 
      suffix[size / 3 - 1]) : (
      new StringBuilder(String.valueOf(number.intValue()))).toString();
  }
  
  public int loopDuration() {
    return 150;
  }
}