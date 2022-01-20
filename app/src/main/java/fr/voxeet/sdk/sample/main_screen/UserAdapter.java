package fr.voxeet.sdk.sample.main_screen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.voxeet.sdk.json.ParticipantInfo;
import com.voxeet.sdk.sample.R;
import com.voxeet.sdk.utils.Opt;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private UserClickListener _listener;

    public void setSelected(ParticipantInfo currentUser) {
        for (ParticipantItem user_item : _user_items) {
            user_item.setSelected(user_item.getParticipantInfo().equals(currentUser));
        }

        notifyDataSetChanged();
    }

    public interface UserClickListener {
        void onUserSelected(ParticipantItem user_item);
    }

    private ParticipantItem[] _user_items;

    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new UserViewHolder(inflater.inflate(R.layout.recycler_main_user_info, parent, false));
    }

    private UserAdapter() {

    }

    public UserAdapter(@NonNull UserClickListener listener, ParticipantItem[] user_items) {
        this();

        _listener = listener;
        _user_items = user_items;
    }

    private void onClickItemAtPosition(int position) {
        if (position < _user_items.length) {
            ParticipantItem user_item = _user_items[position];

            for (ParticipantItem item : _user_items)
                item.setSelected(false);
            user_item.setSelected(true);

            _listener.onUserSelected(user_item);
        }
        notifyDataSetChanged();
    }

    public void reset() {
        for (ParticipantItem item : _user_items) item.setSelected(false);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        holder.apply(_user_items[position]);
    }

    @Override
    public int getItemCount() {
        return _user_items.length;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.username)
        TextView username;

        @Bind(R.id.portrait)
        ImageView portrait;

        @Bind(R.id.background)
        View background;

        ParticipantItem user_info;

        private UserViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void apply(@Nullable ParticipantItem user_info) {
            this.user_info = user_info;
            boolean selected = Opt.of(user_info).then(ParticipantItem::isSelected).or(false);
            ParticipantInfo participantInfo = Opt.of(user_info).then(ParticipantItem::getParticipantInfo).orNull();
            Integer drawable = Opt.of(user_info).then(ParticipantItem::getDrawable).orNull();


            int color;
            if (selected) {
                color = itemView.getContext().getResources().getColor(R.color.grey_light);
            } else {
                color = itemView.getContext().getResources().getColor(R.color.transparent);
            }

            background.setBackgroundColor(color);

            if (null != participantInfo) {
                username.setText(participantInfo.getName());
                if (null != drawable) portrait.setImageResource(drawable);
                portrait.setVisibility(View.VISIBLE);
            } else {
                username.setText("");
                portrait.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position >= 0) onClickItemAtPosition(position);
        }
    }
}
