package logics.life;

/**
 * 
 * this class models a Life System that can only take damage
 *
 */
public class SimpleLifeSystem implements LifeSystem {

	private int health;
	private boolean isDead;

	public SimpleLifeSystem(int health) {
		this.health = health;
		this.isDead = health > 0 ? false : true;
	}

	@Override
	public int getCurrentHealth() {
		return this.health;
	}

	@Override
	public void damage(int damageValue) {
		if (damageValue > 0 && !this.isDead) {
			this.health = (this.health - damageValue) < 0 ? 0 : this.health - damageValue;
			this.isDead = !(this.health > 0);
		}
	}

	@Override
	public Boolean isDead() {
		return this.isDead;
	}

}
