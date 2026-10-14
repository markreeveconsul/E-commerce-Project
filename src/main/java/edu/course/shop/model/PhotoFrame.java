package edu.course.shop.model;
import edu.course.shop.exception.ValidationException;
/** Non-destructive catalog framing; the gallery always retains the full photograph. */
public record PhotoFrame(Mode mode,double x,double y) {
    public enum Mode { AUTO, CONTAIN, COVER }
    public static final PhotoFrame AUTO=new PhotoFrame(Mode.AUTO,.5,.5);
    public PhotoFrame {
        if(mode==null||!Double.isFinite(x)||!Double.isFinite(y)||x<0||x>1||y<0||y>1)
            throw new ValidationException("Choose valid photo framing and a focal point between 0 and 1.");
    }
}
