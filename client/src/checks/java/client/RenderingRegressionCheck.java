package client;

import client.items.ClientEtc;
import common.items.Items;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.function.BooleanSupplier;

/** Exercises the real resources and paint/queue contracts without a screen device. */
final class RenderingRegressionCheck {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void await(BooleanSupplier condition) {
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) throw new AssertionError("Field queue did not complete");
            try { Thread.sleep(5); } catch (InterruptedException ex) { throw new AssertionError(ex); }
        }
    }

    static void run() {
        Point position = new Point(100, 200);
        for (String name : new String[]{"desu", "peasant", "peon", "poring", "tower"}) {
            for (Direction direction : Direction.values()) {
                UnitAnimations owner = new UnitAnimations(name, direction, 1000);
                int dx = switch (direction) {
                    case EAST, NORTH_EAST, SOUTH_EAST -> 100;
                    case WEST, NORTH_WEST, SOUTH_WEST -> -100;
                    default -> 0;
                };
                int dy = switch (direction) {
                    case SOUTH, SOUTH_EAST, SOUTH_WEST -> 100;
                    case NORTH, NORTH_EAST, NORTH_WEST -> -100;
                    default -> 0;
                };
                owner.move(100, 200, 100 + dx, 200 + dy, 1000);
                require(owner.sprite(1000, position, true, false).image != null, name + " movement " + direction);
                Sprite standing = owner.sprite(999, position, false, false);
                require(standing.baseX == 100 && standing.baseY == 200, name + " standing anchor");
                // Exercise horizontal fallbacks, movement, stop, attack and death with one owner.
                owner.move(100, 200, 200, 200, 2000);
                require(owner.sprite(1100, position, true, false) != null, name + " east movement");
                owner.move(100, 200, 0, 200, 2000);
                require(owner.sprite(1100, position, true, false) != null, name + " west movement");
                boolean attacking = owner.attack(direction, 2100);
                require(attacking, "Existing attack event accepted, with explicit static fallback");
                for (long time : new long[]{2099, 2100, 2249, 2250, 2849, 2850, 10000}) {
                    require(owner.sprite(time, position, false, false) != null, "Attack boundary/fallback");
                }
                require(!owner.isAttacking(2850), "Attack finishes without rendering");
                owner.die(3000);
                Sprite first = owner.sprite(3000, position, false, true);
                owner.die(3100);
                if (name.equals("peasant") || name.equals("peon") || name.equals("poring")) {
                    String deathSet = name + "_death";
                    require(owner.sprite(3150, position, false, true).image
                            == DirectionalSpriteSet.load(deathSet).getSprite(direction, 1).image,
                            "Duplicate death must not restart elapsed time");
                }
                require(owner.deathDone(10000), "Death finishes without rendering");
                owner.revive(10000);
                require(owner.sprite(10000, position, false, false) != null, "Revive");
                owner.attack(direction, 11000);
                owner.move(100, 200, 200, 200, 12000);
                require(!owner.isAttacking(11000), "Movement cancels visual attack");
                owner.dispose();
                require(owner.sprite(12000, position, false, false) == null, "Removed owner releases visual state");
            }
        }
        try {
            DirectionalSpriteSet.load("missing_empty_set");
            throw new AssertionError("Unknown set must fail");
        } catch (IllegalArgumentException expected) {
            require(expected.getMessage().contains("missing_empty_set"), "Resource diagnostic names the set");
        }
        try {
            DirectionalSpriteSet.verifyDirections("empty", java.util.Map.of());
            throw new AssertionError("Empty directions must fail before constructing an animator");
        } catch (IllegalArgumentException expected) {
            require(expected.getMessage().contains("Empty sprite set: empty"), "Empty set diagnostic");
        }
        try (GameField field = new GameField()) {
            field.startSelfExecution();
            Drawable marker = solid(0, 0, 0, Color.RED);
            field.asyncAddDrawable(marker);
            field.asyncAddDrawable(marker);
            await(() -> field.getDrawQueue().contains(marker));
            field.asyncRemoveDrawable(marker);
            await(() -> !field.getDrawQueue().contains(marker));
            // Removal proves preceding duplicate addition was executed in FIFO order.
            require(field.getDrawQueue().isEmpty(), "No duplicate registration through queue");

            for (int[] xy : new int[][]{{98, 110}, {118, 110}, {110, 98}, {110, 118}}) {
                Drawable edge = solid(xy[0], xy[1], 130, Color.RED);
                field.addDrawable(edge);
                BufferedImage image = paint(field, 100, 100, 20, 20);
                require(count(image, Color.RED.getRGB()) > 0, "Partially visible sprite at each viewport edge");
                field.removeDrawable(edge);
            }
            Drawable front = solid(110, 110, 120, Color.RED);
            Drawable back = solid(110, 110, 110, Color.BLUE);
            field.addDrawable(front);
            field.addDrawable(back);
            require(paint(field, 100, 100, 20, 20).getRGB(10, 10) == Color.RED.getRGB(), "Depth uses world anchor");
            field.removeDrawable(front);
            field.removeDrawable(back);

            ClientEtc gold = new ClientEtc(900, "Gold", Items.GOLD);
            gold.setX(100); gold.setY(100);
            field.addDrawable(gold);
            Player player = new Player(901, "Long name for paint metrics", 100, .07, 100, 100, Direction.SOUTH, "peasant");
            field.addPlayer(player);
            player.teleportTo(100, 100);
            require(!player.isMove() && player.getCurPos().equals(new Point(100, 100)), "Zero-distance teleport retains coordinates");
            player.move(100, 100, 200, 100, WUnit.now());
            player.changeSpriteSet("peon");
            require(player.getSprite() != null, "Changing set during movement initializes the replacement owner");
            player.teleportTo(100, 100);
            player.setText("Paint context text wraps correctly");
            BufferedImage image = paint(field, 0, 0, 250, 200);
            require(countOpaque(image) > 100, "Unit and gold visible before any GUI paint");
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                Sprite small = UnitPainter.sprite(player, graphics);
                graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
                Sprite large = UnitPainter.sprite(player, graphics);
                require(large.image.getWidth() > small.image.getWidth(), "Current paint font metrics");
                require(large.baseY == 100 && large.y + large.image.getHeight() == 100, "Name preserves foot anchor");
                field.drawTextClouds(graphics, 0, 0);
            } finally { graphics.dispose(); }
            field.delPlayer(player.getID());
            require(player.getSprite() == null && player.getSelectedUnit() == null, "Removed player visual state");
            require(!field.getPlayers().contains(player) && !field.getUnits().contains(player), "Removed player leaves every owner collection");
            field.removeDrawable(gold);

            // Projectiles and effects stay in the very same Drawable pass.
            Player attacker = new Player(902, "attacker", 100, .07, 50, 100, Direction.EAST, "peasant");
            Player target = new Player(903, "target", 100, .07, 180, 100, Direction.WEST, "peon");
            attacker.selectUnit(target);
            PeasantNuke projectile = new PeasantNuke(attacker);
            projectile.use(WUnit.now());
            field.addDrawable(projectile);
            require(countOpaque(paint(field, 0, 0, 250, 200)) > 0, "Projectile visible");
            field.removeDrawable(projectile);
            Tower shooter = new Tower(905, "tower", 100, 10, 1000, 50, 100, Direction.EAST, "tower");
            shooter.selectUnit(target);
            CanonNuke cannon = new CanonNuke(shooter);
            cannon.use(WUnit.now());
            shooter.selectUnit(null);
            require(cannon.getSprite() != null, "In-flight cannon resolves its launch target independently of selection");
            PeasantNuke delayed = new PeasantNuke(attacker);
            delayed.use(WUnit.now() + 100);
            require(delayed.getSprite() == null, "Projectile stays hidden before its launch time");
            try { Thread.sleep(105); } catch (InterruptedException ex) { throw new AssertionError(ex); }
            require(delayed.getSprite() != null, "Projectile visible at launch, no doubled delay");
            try { Thread.sleep(150); } catch (InterruptedException ex) { throw new AssertionError(ex); }
            require(delayed.getSprite() == null && cannon.getSprite() == null, "Finished projectiles leave no last frame");
            CanonHitAnimation hit = new CanonHitAnimation("canon", position, Direction.EAST, WUnit.now());
            field.addHitAnimation(hit);
            require(countOpaque(paint(field, 0, 0, 250, 250)) > 0, "Hit effect visible and anchored");
            CanonHitAnimation ended = new CanonHitAnimation("canon", position, Direction.EAST, WUnit.now() - 1000);
            field.addHitAnimation(ended);
            paint(field, 0, 0, 250, 250); // observes completion
            paint(field, 0, 0, 250, 250); // removes completed effect
            require(!field.getDrawQueue().contains(ended), "Completed effect leaves draw queue");
            Monster dead = new Monster(904, "dead", 20, .08, 100, 200, Direction.SOUTH, "poring");
            field.addMonster(dead);
            dead.kill();
            try { Thread.sleep(800); } catch (InterruptedException ex) { throw new AssertionError(ex); }
            field.delDeadMonsters();
            require(!field.getDrawQueue().contains(dead), "Dead monster removed even without painting");
        }
        System.out.println("PASS: support table, animation transitions, paint metrics, clipping, depth, queue lifecycle and effects");
    }

    private static BufferedImage paint(GameField field, int x, int y, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            ZBuffer buffer = new ZBuffer();
            Dimension size = new Dimension(width, height);
            field.updateZBuffer(buffer, graphics, x, y, size);
            buffer.draw(graphics, x, y, size);
        } finally { graphics.dispose(); }
        return image;
    }

    private static Drawable solid(int x, int y, int depth, Color color) {
        return new Drawable() {
            public Sprite getSprite() {
                Sprite sprite = new Sprite();
                sprite.image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = sprite.image.createGraphics();
                graphics.setColor(color); graphics.fillRect(0, 0, 4, 4); graphics.dispose();
                sprite.x = x; sprite.y = y; sprite.baseY = depth;
                return sprite;
            }
            public void draw(Graphics graphics, int cx, int cy, Dimension size) {}
        };
    }

    private static int count(BufferedImage image, int rgb) {
        int count = 0;
        for (int x = 0; x < image.getWidth(); x++) for (int y = 0; y < image.getHeight(); y++) {
            if (image.getRGB(x, y) == rgb) count++;
        }
        return count;
    }

    private static int countOpaque(BufferedImage image) {
        int count = 0;
        for (int x = 0; x < image.getWidth(); x++) for (int y = 0; y < image.getHeight(); y++) {
            if ((image.getRGB(x, y) >>> 24) != 0) count++;
        }
        return count;
    }
}
