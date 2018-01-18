package sdk.voxeet.com.toolkit.views.uitookit.sdk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.voxeet.toolkit.R;

/**
 * Created by romainbenmansour on 10/05/2017.
 */

public class VoxeetConferenceBarView2 extends VoxeetView {

    private LinearLayout container;

    private final String TAG = VoxeetConferenceBarView2.class.getSimpleName();

    public VoxeetConferenceBarView2(Context context) {
        super(context);
    }

    public VoxeetConferenceBarView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VoxeetConferenceBarView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private VoxeetConferenceBarView2 addButton(int drawable, OnClickListener listener) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(drawable);

        // setting layout params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(10, 10, 10, 10);

        imageView.setLayoutParams(params);

        imageView.setPadding(10, 10, 10, 10);

        // listener
        imageView.setOnClickListener(listener);

        container.addView(imageView);

        return this;
    }

    @Override
    public void init() {

    }

    @Override
    protected int layout() {
        return R.layout.voxeet_conference_bar_view_2;
    }

    @Override
    protected void bindView(View view) {
        container = view.findViewById(R.id.container);
    }

//    public static class Builder {
//
//        private class ConferenceBarComponent {
//
//            int drawable;
//
//            OnClickListener listener;
//
//            ConferenceBarComponent(int drawable, OnClickListener listener) {
//                this.drawable = drawable;
//                this.listener = listener;
//            }
//        }
//
//        private FrameLayout.LayoutParams params;
//
//        private Context context;
//
//        private List<ConferenceBarComponent> components;
//
//        public Builder with(Context context) {
//            this.components = new ArrayList<>();
//
//            this.context = context;
//
//            return this;
//        }
//
//        public Builder setLayoutParams(LayoutParams params) {
//            this.params = params;
//
//            return this;
//        }
//
//        public Builder hangUp(int drawable, OnClickListener listener) {
//            components.add(new ConferenceBarComponent(drawable, listener));
//
//            return this;
//        }
//
//        public Builder speaker(int drawable, OnClickListener listener) {
//            components.add(new ConferenceBarComponent(drawable, listener));
//
//            return this;
//        }
//
//        public Builder mute(int drawable, OnClickListener listener) {
//            components.add(new ConferenceBarComponent(drawable, listener));
//
//            return this;
//        }
//
//        public Builder record(int drawable, OnClickListener listener) {
//            components.add(new ConferenceBarComponent(drawable, listener));
//
//            return this;
//        }
//
//        public Builder video(int drawable, OnClickListener listener) {
//            components.add(new ConferenceBarComponent(drawable, listener));
//
//            return this;
//        }
//
//        public VoxeetConferenceBarView2 build() {
//            return new VoxeetConferenceBarView2(context, this);
//        }
//    }
}
