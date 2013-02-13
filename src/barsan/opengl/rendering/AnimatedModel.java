package barsan.opengl.rendering;

public class AnimatedModel {

	class Frame {
		// interpolations would also go here in the future
		
		// How long does this frame influence the animation
		public final float duration;
		
		// The actual keyframe
		public final Model model;
		
		// The smaller this is, the more weight the next frame has
		public float durationLeft;
		
		public Frame(Model model, float duration) {
			this.model = model;
			this.duration = duration;
		}
	}
}
