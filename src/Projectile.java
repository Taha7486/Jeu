import java.awt.*;

public class Projectile {
    private int x, y;
    private final int speed = 10;
    private final int width = 5;
    private final int height = 15;
    private boolean active = true;
    private final Color color;
    private final boolean isPlayer1Projectile;

    public Projectile(int startX, int startY, boolean isPlayer1Projectile) {
        this.x = startX - width / 2;
        this.y = startY;
        this.isPlayer1Projectile = isPlayer1Projectile;
        this.color = isPlayer1Projectile ? Color.YELLOW : Color.CYAN;
    }

    public void update() {
        if (isPlayer1Projectile) {
            y -= speed; // Player 1 projectiles go up
        } else {
            y += speed; // Player 2 projectiles go down
        }

        if (y < 0 || y > 600) {
            active = false;
        }
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPlayer1Projectile() {
        return isPlayer1Projectile;
    }
}