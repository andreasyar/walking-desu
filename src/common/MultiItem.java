package common;

/**
 * Abstract item what contains many same items inself.
 * For example 10 arrows. 9000 dollars.
 * 10 arrows is one item -- arrow what repeats 10 times.
 */
public abstract class MultiItem extends Item {

    public MultiItem(long id, String name) {
        super(id, name);
    }

    
}
