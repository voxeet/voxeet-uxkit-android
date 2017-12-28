package fr.voxeet.sdk.sample.main_screen;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.voxeet.sdk.sample.R;
import voxeet.com.sdk.json.UserInfo;

/**
 * Created by kevinleperf on 24/11/2017.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private UserClickListener _listener;

    public void setSelected(UserInfo currentUser) {
        for (UserItem user_item : _user_items) {
            user_item.setSelected(user_item.getUserInfo().equals(currentUser));
        }

        notifyDataSetChanged();
    }

    public interface UserClickListener {
        void onUserSelected(UserItem user_item);
    }

    private UserItem[] _user_items;

    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new UserViewHolder(inflater.inflate(R.layout.recycler_main_user_info, parent, false));
    }

    private UserAdapter() {

    }

    public UserAdapter(@NonNull UserClickListener listener, UserItem[] user_items) {
        this();

        _listener = listener;
        _user_items = user_items;
    }

    private void onClickItemAtPosition(int position) {
        if (position < _user_items.length) {
            UserItem user_item = _user_items[position];

            for (UserItem item : _user_items)
                item.setSelected(false);
            user_item.setSelected(true);

            _listener.onUserSelected(user_item);
        }
        notifyDataSetChanged();
    }

    public void reset() {
        for (UserItem item : _user_items) item.setSelected(false);
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

        UserItem user_info;

        private UserViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void apply(UserItem user_info) {
            this.user_info = user_info;


            int color;
            if (user_info.isSelected()) {
                color = itemView.getContext().getResources().getColor(R.color.grey_light);
            } else {
                color = itemView.getContext().getResources().getColor(R.color.transparent);
            }

            background.setBackgroundColor(color);

            if (user_info != null && user_info.getUserInfo() != null) {
                username.setText(user_info.getUserInfo().getName());
                portrait.setImageResource(user_info.getDrawable());
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
