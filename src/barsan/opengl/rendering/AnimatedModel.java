package barsan.opengl.rendering;

import java.util.ArrayList;
import java.util.List;

public class AnimatedModel {
	
	class Frame {
		// interpolations would also go here in the future
		
		// How long does this frame influence the animation
		public final float duration;
		
		// The actual keyframe
		public final StaticModel model;
		
		public Frame(StaticModel model, float duration) {
			this.model = model;
			this.duration = duration;
		}
	}
	
	String name;
	private List<Frame> frames = new ArrayList<>();
	
	
}
