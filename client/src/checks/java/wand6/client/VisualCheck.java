package wand6.client;

import java.awt.Point;

public final class VisualCheck {
    public static void main(String[] args) throws Exception {
        var walk = DirectionalSpriteSet.load("peasant_walk");
        var stand = DirectionalSpriteSet.load("peasant_stand");
        int[][] offsets = {{100,0},{100,-100},{0,-100},{-100,-100},{-100,0},{-100,100},{0,100},{100,100}};
        int i=0;
        for (var direction:Direction.values()) {
            int[] offset=offsets[i++];
            require(Direction.getDirection(0,0,offset[0],offset[1]) == direction,"direction");
            require(walk.getSpriteCount(direction)==4 && stand.getSpriteCount(direction)==1,"frames");
            var animation = new MovementAnimation("peasant");
            animation.run(100,100,100+offset[0],100+offset[1],10);
            var first=animation.getSprite(new Point(100,100));
            var next=animation.getSprite(new Point(110,100));
            require(first.image != next.image,"walking frame progresses");
            require(first.image.getWidth()==56 && first.x==72 && first.y==44,"same 3.2 crop and foot anchor");
        }
        var attack = DirectionalSpriteSet.load("peasant_attack");
        var death = DirectionalSpriteSet.load("peasant_death");
        var bolt = DirectionalSpriteSet.load("peasant_bolt");
        for (var direction : Direction.values()) {
            require(attack.getSpriteCount(direction) == 5 && death.getSpriteCount(direction) == 3
                    && bolt.getSpriteCount(direction) == 1, "combat directional frames");
            var cast = new UseSkillAnimation("peasant");
            cast.run(direction,100);
            require(cast.isPlaying(849) && !cast.isPlaying(850) && cast.getNukeAnimationDelay() == 375, "3.2 attack timeline");
            require(cast.getSprite(100,new Point(80,90)).image != cast.getSprite(400,new Point(80,90)).image, "attack progresses");
            cast.stop(); require(!cast.isPlaying(200), "attack cancelled");
            var dying = new DeathAnimation("peasant");
            dying.run(direction,100);
            require(!dying.isDone(549) && dying.isDone(550), "death ends at 450ms");
            require(dying.getSprite(10000,new Point(80,90)).image == death.getSprite(direction,2).image, "death holds final frame");
        }
        for (var direction : Direction.values()) {
            require(DirectionalSpriteSet.load("poring_walk").getSpriteCount(direction) == 4
                    && DirectionalSpriteSet.load("poring_death").getSpriteCount(direction) == 5
                    && DirectionalSpriteSet.load("tower_stand").getSpriteCount(direction) == 1
                    && DirectionalSpriteSet.load("canon_bolt").getSpriteCount(direction) == 3, "NPC directions and frames");
            var dying = new DeathAnimation("poring"); dying.run(direction,100);
            require(!dying.isDone(849) && dying.isDone(850), "poring death 750ms");
        }
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            var manager = PlayerManager.getInstance();
            manager.createSelfPlayer(1,"check","peasant");
            manager.defenceStatus(new wand6.server.messages.TowerDefenceStatusMessage(0,1,0,100,1));
            manager.appearEntity(new wand6.server.messages.AppearEntityMessage(2,wand6.common.Unit.Kind.MONSTER,
                    "Monster","poring",new wand6.server.messages.MoveMessage(2,700,100,705,755,0),.08,20,20,null));
            manager.die(new wand6.server.messages.DeathMessage(2,0,700,300,100));
            manager.removeEntity(2,true);
            require(manager.corpseCount() == 1,"killed NPC keeps death animation after removal");
            manager.defenceStatus(new wand6.server.messages.TowerDefenceStatusMessage(1,1,0,100,1));
            require(manager.corpseCount() == 0 && manager.size() == 1,"reset clears NPC corpse and preserves player");
            manager.disconnected();
        });
        try { DirectionalSpriteSet.load("missing_walk"); throw new AssertionError("unknown set accepted"); }
        catch(IllegalArgumentException expected) { }
        var camera=Camera.follow(Integer.MAX_VALUE,Integer.MAX_VALUE,1000,720);
        require((long)camera.x()+1000==(long)wand6.common.MapFragment.maxX+1,"far boundary");
        // Shoreline lookup at a fragment edge uses global neighbors, not fictitious ocean.
        int[][] h=new int[32][32];
        for(int y=0;y<32;y++)for(int x=0;x<32;x++)h[y][x]=wand6.common.Terrain.height(x,y);
        var fragment = new CMapFragment(h,0,0);
        int[][] full = new int[64][64];
        for(int y=0;y<64;y++)for(int x=0;x<64;x++)full[y][x]=wand6.common.Terrain.height(x,y);
        var reference = new CMapFragment(full,0,0);
        var tile = CMapFragment.class.getDeclaredMethod("getTileKey",int.class,int.class);
        tile.setAccessible(true);
        for(int cy=0;cy<2;cy++) for(int cx=0;cx<2;cx++) {
            int[][] chunk=new int[32][32];
            for(int y=0;y<32;y++)for(int x=0;x<32;x++)chunk[y][x]=full[cy*32+y][cx*32+x];
            var separate = new CMapFragment(chunk,cx,cy);
            for(int y=0;y<32;y++)for(int x=0;x<32;x++) {
                require(tile.invoke(separate,x,y).equals(tile.invoke(reference,cx*32+x,cy*32+y)),
                        "separate chunks choose same shoreline tiles as a contiguous map");
            }
        }
        var image = new java.awt.image.BufferedImage(1024,1024,java.awt.image.BufferedImage.TYPE_INT_RGB);
        var graphics=image.createGraphics();
        try { fragment.draw(graphics,0,0,new java.awt.Dimension(1024,1024)); } finally { graphics.dispose(); }
        System.out.println("PASS: combat sets, attack/death timing and cancellation, eight sprite directions, walking frames, standing sets, crop/anchor, unknown set, boundary camera, contiguous/chunk shoreline equivalence, atlas rendering.");
    }
    private static void require(boolean condition,String label) { if(!condition)throw new AssertionError(label); }
}
