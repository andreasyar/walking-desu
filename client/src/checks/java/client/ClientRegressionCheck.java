package client;

import client.items.ClientEtc;
import common.WanderingServerTime;
import common.items.Items;
import java.awt.Point;

/** Resource-backed checks, runnable without a display server. */
public final class ClientRegressionCheck {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        for (String name : new String[]{"abc", "abcdefghij", "stage5-one"}) require(common.PlayerName.isValid(name), "Valid nickname boundary");
        for (String name : new String[]{"", "ab", "abcdefghijk", "bad\"nick", "bad\nnick"}) require(!common.PlayerName.isValid(name), "Rejected nickname");
        System.setProperty("walkingdesu.testNick", "ignored");
        require(ClientTestMode.nickname() == null, "Test nickname alone cannot skip login");
        System.setProperty("walkingdesu.testMode", "true");
        System.setProperty("walkingdesu.testNick", "stage5-one");
        require(ClientTestMode.nickname().equals("stage5-one"), "Explicit test nickname");
        System.setProperty("walkingdesu.testNick", "elevenchars");
        try { ClientTestMode.nickname(); throw new AssertionError("Invalid test nickname accepted"); }
        catch (IllegalArgumentException expected) { }
        System.clearProperty("walkingdesu.testNick");
        try { ClientTestMode.nickname(); throw new AssertionError("Missing test nickname accepted"); }
        catch (IllegalArgumentException expected) { }
        System.clearProperty("walkingdesu.testMode");
        WanderingServerTime.getInstance().setServerTime(System.currentTimeMillis());
        ServerInteraction.serverStartTime = WanderingServerTime.getInstance().getServerStartTime();
        for (Direction direction : Direction.values()) {
            DeathAnimation animation = new DeathAnimation("peasant");
            animation.getSprite(0, new Point(100, 200)); // safe even before explicit run
            animation.run(direction, 1000);
            for (long time : new long[] {999, 1000, 1149, 1150, 1450, 10000}) {
                Sprite sprite = animation.getSprite(time, new Point(100, 200));
                require(sprite.baseX == 100 && sprite.baseY == 200, "Death frame anchor");
                require(sprite.y == 200 - sprite.image.getHeight(), "Death frame position");
            }
            require(animation.isDone(), "Death animation must finish");
            DirectionalSpriteSet frames = DirectionalSpriteSet.load("peasant_death");
            animation.run(direction, 1000);
            long duration = 150L * frames.getSpriteCount(direction);
            require(animation.getSprite(1000, new Point()).image == frames.getSprite(direction, 0).image,
                    "First death frame");
            require(animation.getSprite(1000 + duration, new Point()).image
                    == frames.getSprite(direction, frames.getSpriteCount(direction) - 1).image,
                    "Final frame at exact duration boundary");
            Player player = new Player(1, "victim", 100, .07, 100, 200, direction, "peasant");
            player.move(100, 200, 200, 300, 0);
            player.doHit(50);
            require(!player.dead(), "First hit must not kill");
            player.doHit(50);
            require(player.dead() && !player.isMove(), "Lethal hit must start death and stop movement");
            player.spriteAt(0); // before deathplayer packet
            player.teleportTo(0, 0);
            player.kill(); // duplicate deathplayer must not restart animation
            player.spriteAt(10000);
            require(player.spriteAt(10000).image == DirectionalSpriteSet.load("peasant_death").getSprite(Direction.SOUTH_EAST, 2).image, "Death completion and duplicate death retains frame");
            player.resurect();
            require(!player.dead(), "Respawn restores health");
            player.doHit(100);
            require(!player.deathAnimationDone(), "Next death starts a fresh animation");
        }
        for (Direction direction : Direction.values()) {
            DeathAnimation animation = new DeathAnimation("poring");
            animation.getSprite(0, new Point(100, 200));
            DirectionalSpriteSet frames = DirectionalSpriteSet.load("poring_death");
            animation.run(direction, 1000);
            long duration = 150L * frames.getSpriteCount(direction);
            require(animation.getSprite(999, new Point()).image == frames.getSprite(direction, 0).image,
                    "Monster first frame before start");
            for (int frame = 0; frame < frames.getSpriteCount(direction); frame++) {
                Sprite sprite = animation.getSprite(1000 + frame * 150L, new Point(100, 200));
                require(sprite.image == frames.getSprite(direction, frame).image, "Monster death frame order");
                require(sprite.baseX == 100 && sprite.baseY == 200, "Monster death frame anchor");
            }
            Sprite last = animation.getSprite(1000 + duration, new Point(100, 200));
            require(animation.isDone() && last.y == 200 - last.image.getHeight(), "Monster final frame");
            Monster monster = new Monster(3, "victim", 20, .08, 100, 200, direction, "poring");
            monster.move(100, 200, 200, 300, 0);
            monster.doHit(10);
            require(!monster.dead(), "Nonlethal monster hit");
            monster.doHit(10);
            require(monster.dead() && !monster.isMove(), "Lethal hit stops monster and starts death");
            monster.spriteAt(0); // before deathmonster packet
            monster.kill();
            monster.spriteAt(10000);
            require(monster.spriteAt(10000).image == frames.getSprite(Direction.SOUTH_EAST, frames.getSpriteCount(Direction.SOUTH_EAST) - 1).image, "Duplicate deathmonster retains final frame");
        }
        ClientEtc gold = new ClientEtc(2, "Gold", Items.GOLD, 1);
        gold.setX(200);
        gold.setY(300);
        Sprite sprite = gold.getSprite();
        require(gold.getW() > 0 && gold.getH() > 0, "Gold bounds must have size");
        require(sprite.x == 200 - gold.getW() / 2 && sprite.y == 300 - gold.getH() / 2,
                "Gold sprite must be centered on its world coordinates");
        for (int x = 0; x < sprite.image.getWidth(); x++) {
            for (int y = 0; y < sprite.image.getHeight(); y++) {
                require(gold.onItem(sprite.x + x, sprite.y + y), "Every rendered gold pixel must be clickable");
            }
        }
        require(!gold.onItem(sprite.x - 1, sprite.y), "Outside left edge");
        require(!gold.onItem(sprite.x + gold.getW(), sprite.y), "Outside right edge");
        require(!gold.onItem(sprite.x, sprite.y + gold.getH()), "Outside bottom edge");
        RenderingRegressionCheck.run();
        ItemClientCheck.run();
        System.out.println("PASS: moving player death/respawn, monster death in all directions; gold sprite hit bounds");
    }
}
