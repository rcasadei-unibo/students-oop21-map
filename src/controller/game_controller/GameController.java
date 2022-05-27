package controller.game_controller;

import controller.ActionFlag;
import controller.GameAreaController;
import controller.PageController;
import model.game_object.artefact.Artefact;
import model.game_object.entity.Player;
import model.game_object.entity.SimpleEnemy;
import model.game_statistics.GameStatisticsImpl;
import utilities.Pair;
import utilities.RoomConstant;
import view.game.TotalPanel;

/**
 * 
 * Manage Game Loop methods
 * 
 */
public abstract class GameController {

  private final GameAreaController gameAreaController;
  private final TotalPanel totalPanel;
  private final PageController pageController;
  private ActionFlag flag;
  private int currentActionNumber;
  private final GameStatisticsImpl gameStats;

  public GameController(final GameAreaController gameAreaController, final TotalPanel totalPanel,
      final PageController pageController, final GameStatisticsImpl gameStats) {
    this.totalPanel = totalPanel;
    this.pageController = pageController;
    this.gameStats = gameStats;
    this.gameAreaController = gameAreaController;
    this.currentActionNumber = this.getPlayer().getActionNumber();
  }

  /**
   * start a new Enemy Turn.
   */
  public abstract void enemyTurn();

  /**
   * 
   * @return if the door of the room is available
   */
  public abstract boolean isDoorAvailable();

  /**
   * 
   * @return if the player won the game
   */
  public abstract boolean isWon();

  public GameStatisticsImpl getGameStats() {
    return this.gameStats;
  }

  public PageController getPageController() {
    return this.pageController;
  }

  /**
   * Decrease the number of available action
   */
  public void decreaseAction() {
    this.getTotalPanel().getScrollableStats().getStatsValues().update(this.getPlayer(), this.getGameStats(),
        this.currentActionNumber);
    this.currentActionNumber--;
    this.getTotalPanel().getScrollableStats().getStatsValues().update(this.getPlayer(), this.getGameStats(),
        this.currentActionNumber);
  }

  /**
   * Skip the turn
   */
  public void skipTurn() {
    this.totalPanel.getGameArea().removeHighlight();
    this.resetActionNumber();
    this.getTotalPanel().getScrollableStats().getStatsValues().update(this.getPlayer(), this.getGameStats(),
        this.currentActionNumber);
    this.enemyTurn();
  }

  /**
   * Attack in a chosen cell by the user
   */
  private void attack(final Pair<Integer, Integer> pos) {
    final SimpleEnemy enemy = RoomConstant.searchEnemy(pos, this.totalPanel.getGameArea().getRoom().getEnemyList());
    this.getTotalPanel().getScrollableStats().getStatsValues().update(this.getPlayer(), this.getGameStats(),
        this.currentActionNumber);
    if (enemy != null) {
      enemy.damage(this.getPlayer().getWeapon().getDamage());
      if (enemy.isDead()) {
        this.totalPanel.getGameArea().removeGameObject(pos);
        this.gameStats.increaseKilledEnemies();
        this.getTotalPanel().getScrollableStats().getStatsValues().update(this.getPlayer(), this.gameStats,
            this.currentActionNumber);
      }
      this.getTotalPanel().getScrollableLog().getLogMessage().update(enemy.getName() + " got hit");
    } else {
      this.getTotalPanel().getScrollableLog().getLogMessage().update(this.getPlayer().getName() + " miss the attack");
    }
    this.endPlayerTurn();
  }

  /**
   * Move in a chosen cell by the user
   * 
   * @param newpos : the new position of the player
   */
  private void move(final Pair<Integer, Integer> newpos) {
    this.getTotalPanel().getScrollableStats().getStatsValues().update(this.getPlayer(), this.getGameStats(),
        this.currentActionNumber);
    if (RoomConstant.searchEnemy(newpos, this.totalPanel.getGameArea().getRoom().getEnemyList()) == null) {
      final Artefact artefact = RoomConstant.searchArtefact(newpos,
          this.totalPanel.getGameArea().getRoom().getArtefactList());
      if (artefact != null) {
        artefact.execute(this.getPlayer());
        this.totalPanel.getGameArea().removeGameObject(newpos);
        this.gameStats.increaseCollectedArtefacts();
        this.getTotalPanel().getScrollableLog().getLogMessage().update("Picked up " + artefact.getName() + ".");
        this.getTotalPanel().getScrollableStats().getStatsValues().update(this.getPlayer(), this.gameStats,
            this.currentActionNumber);
      }
      this.totalPanel.getGameArea().moveGameObject(this.getPlayer().getPos(), newpos);
      if (this.totalPanel.getGameArea().getRoom().playerOnDoor()) {
        this.gameStats.increaseCompletedRooms();
        this.getTotalPanel().getScrollableStats().getStatsValues().update(this.getPlayer(), this.gameStats,
            this.currentActionNumber);
        this.totalPanel.getGameArea().changeRoom(this.gameAreaController.generateNewRoom(this.getPlayer()));
      }
    }
    this.endPlayerTurn();
  }

  /**
   * 
   * @return the player
   */
  public Player getPlayer() {
    return this.totalPanel.getGameArea().getRoom().getPlayer();
  }

  /**
   * 
   * @return if the player is dead
   */
  public boolean isDead() {
    return this.getPlayer().isDead();
  }

  /**
   * 
   * @return the total panel
   */
  public TotalPanel getTotalPanel() {
    return this.totalPanel;
  }

  /**
   * Specify the type of action to apply to GameArea's chosen cell.
   * 
   * @param actionFlag : set the ActionFlag
   */
  public void setFlag(final ActionFlag flag) {
    this.flag = flag;
    this.totalPanel.getGameArea().removeHighlight();
    this.totalPanel.getGameArea().highlightCells(this.flag);
  }

  /**
   * 
   * @return the number of action of the player
   */
  public int getCurrentActionNumber() {
    return currentActionNumber;
  }

  /**
   * reset the number of action available by the player
   *
   */
  public void resetActionNumber() {
    this.currentActionNumber = this.getPlayer().getActionNumber();
  }

  /**
   * end the turn of the player
   */
  private void endPlayerTurn() {
    this.totalPanel.getGameArea().removeHighlight();
    this.decreaseAction();
    if (this.getCurrentActionNumber() == 0) {
      enemyTurn();
    }
    if (isWon()) {
      this.getPageController().showVictory();
    } else if (isDefeated()) {
      this.getPageController().showDefeat();
    }
  }

  /**
   * 
   * @param pos the pos of the player
   */
  public void makeAction(final Pair<Integer, Integer> pos) {
    if (this.flag.equals(ActionFlag.ATTACK)) {
      this.attack(pos);
      this.gameStats.increaseAttackActionCounter();
      this.getTotalPanel().getScrollableStats().getStatsValues().update(this.getPlayer(), this.getGameStats(),
          this.currentActionNumber);
    } else if (this.flag.equals(ActionFlag.MOVE)) {
      this.move(pos);
      this.gameStats.increaseMoveActionCounter();
      this.getTotalPanel().getScrollableStats().getStatsValues().update(this.getPlayer(), this.getGameStats(),
          this.currentActionNumber);
    }

  }

  public abstract boolean isDefeated();

}
