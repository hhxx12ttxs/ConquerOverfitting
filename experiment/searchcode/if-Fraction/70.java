package mt.comic.gui.swing.scroller;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;

/**
 * Controller for animation of the {@link Scroller}. <p>
 *
 * Controller is completely driven by time and current state.
 *
 * @author marko.talijanac@gmail.com
 */
@RequiredArgsConstructor
public class ScrollerAnimationController {

    public interface ScrollAnimation {
        int  getPositionX();
        int  getPositionY();
        void setPositionXY( int x, int y );
        void updateScreen();
    }

    private long fadeinDuration  = 100;
    private long scrollDuration  = 100;
    private long waitingDuration = 1000;
    private long fadeoutDuration = 100;

    @Setter private long interuptableAfterScrollingInMs = 0;

    private int stillPositionY = 0;
    private int fadedPositionY = 200;

    @NonNull private ScrollAnimation animation;

    public void setDurations( long fadeInDuration, long scrollDuration, long waitingDuration, long fadeOutDuration ) {
        this.fadeinDuration = fadeInDuration;
        this.scrollDuration = scrollDuration;
        this.waitingDuration = waitingDuration;
        this.fadeoutDuration = fadeOutDuration;
    }

    public void setFadeInFadeOutEndPositions( int stillPositionY, int fadedPositionY ) {
        this.stillPositionY = stillPositionY;
        this.fadedPositionY = fadedPositionY;
    }

    private class ScrollTimingTarget extends TimingTargetAdapter {
        @Getter private boolean interuptable;
        private double interruptibleLimit;

        private final double fadeInEndsFraction;
        private final double scrollEndsFraction;
        private final double waitingEndsFraction;

        @Getter private final long duration;

        private final int startPosX, startPosY, endPosX;

        public ScrollTimingTarget( int startPosX, int startPosY, int endPosX ) {
            this.startPosX = startPosX;
            this.startPosY = startPosY;
            this.endPosX = endPosX;

            int absYDistance = 0;
            long fadeInDuration = 0;

            if( startPosY != stillPositionY ) {
                int totalYDistance = Math.abs( stillPositionY - fadedPositionY );
                absYDistance = Math.abs( stillPositionY - startPosY );
                double yRemainingDistanceFraction = (double) absYDistance / (double) totalYDistance;
                fadeInDuration = (long) (fadeinDuration * yRemainingDistanceFraction);
            }

            duration = fadeInDuration + scrollDuration + waitingDuration + fadeoutDuration;
            fadeInEndsFraction = (double) fadeInDuration / duration;
            scrollEndsFraction = (double) ( fadeInDuration + scrollDuration ) / duration;
            waitingEndsFraction = (double) ( fadeInDuration + scrollDuration + waitingDuration ) / duration;

            long interuptableAfter = fadeInDuration + scrollDuration + interuptableAfterScrollingInMs;
            interruptibleLimit = (double) interuptableAfter / duration;
        }

        @Override
        public void timingEvent( Animator source, double fraction ) {
            if( fraction <= fadeInEndsFraction ) {
                fadeIn( fraction );
            }
            else if( fraction <= scrollEndsFraction ) {
                scroll( fraction );
            }
            else if( fraction <= waitingEndsFraction) {
                wait( fraction );
            }
            else {
                fadeOut( fraction );
            }
            animation.updateScreen();

            interuptable = fraction > interruptibleLimit;
        }

        private void fadeIn( final double fraction ) {
           double fadingInDurationFraction = fraction / fadeInEndsFraction;
           int posY = (int) ( ( stillPositionY - startPosY ) * fadingInDurationFraction ) + startPosY;
           animation.setPositionXY( startPosX, posY );
        }

        private void scroll( final double fraction ) {
            double currentScrollDurationFraction = fraction - fadeInEndsFraction;
            double totalScrollDurationFraction = scrollEndsFraction - fadeInEndsFraction;
            double normalizedFraction = currentScrollDurationFraction / totalScrollDurationFraction;
            int posX = (int) ( ( endPosX - startPosX ) * normalizedFraction ) + startPosX;
            animation.setPositionXY( posX, stillPositionY );
        }

        private void wait( final double fraction ) {
            animation.setPositionXY( endPosX, stillPositionY );
        }

        private void fadeOut( final double fraction ) {
            double currentFadeoutDurationFraction = fraction - waitingEndsFraction;
            double totalFadeOutDurationFraction = 1.0d - waitingEndsFraction;
            double normalizedFraction = currentFadeoutDurationFraction / totalFadeOutDurationFraction;
            int posY = ( int ) ( ( fadedPositionY - stillPositionY ) * normalizedFraction ) + stillPositionY;
            animation.setPositionXY( endPosX, posY );
        }

        @Override
        public void end(Animator source) {
            animation.setPositionXY( endPosX, fadedPositionY );
            animationControl = null;
            animation.updateScreen();
        }
    }


    @AllArgsConstructor @Getter
    private class AnimationControl {
        Animator animator;
        ScrollTimingTarget target;
    }

    private AnimationControl animationControl = null;

    public synchronized boolean scroll( int scrollToPosX ) {
        // da li se neka animacija trenutno uopće postoji:
        if( animationControl != null ) {
            // ako postoji da li se može zaustaviti:
            //      ako se ne može zaustaviti vrati false
            if( ! animationControl.getTarget().isInteruptable() ) {
                return false;
            }

            // zaustavi animaciju
            animationControl.getAnimator().stop();
        }

        // dohvati trenutnu poziciju na ekranu
        int curPosX = animation.getPositionX();
        int curPosY = animation.getPositionY();

        // kreriraj novu animaciju
        ScrollTimingTarget timingTarget = new ScrollTimingTarget( curPosX, curPosY, scrollToPosX );

        Animator animator = new Animator.Builder()
            .setDuration( timingTarget.getDuration(), TimeUnit.MILLISECONDS )
            .addTarget( timingTarget )
            .build();

        // pokreni novu animaciju
        AnimationControl acontrol = new AnimationControl( animator, timingTarget );
        animationControl = acontrol;
        animationControl.getAnimator().start();
        return true;
    }

}

