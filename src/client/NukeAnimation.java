package client;

/**
 * Абстрактный класс анимации нюка. Каждая анимация нюка имеет набор направленных
 * спрайтов, которые и составляют анимацию.
 */
public abstract class NukeAnimation {

    /**
     * Набор направленных спрайтов.
     */
    protected final DirectionalSpriteSet dsSet;

    protected NukeAnimation(String dsSet) {
        this.dsSet = DirectionalSpriteSet.load(dsSet);
    }
}
