package fr.voxeet.sdk.sample.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.squareup.picasso.Picasso;
import com.voxeet.android.media.MediaStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.voxeet.sdk.sample.R;
import sdk.voxeet.com.toolkit.views.android.RoundedImageView;
import sdk.voxeet.com.toolkit.views.uitookit.nologic.VideoView;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.json.UserInfo;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;

/**
 * Created by RomainB on 4/21/16.
 */
public class ParticipantAdapter extends BaseAdapter {
    private static final String TAG = ParticipantAdapter.class.getSimpleName();

    private Context context;

    private List<DefaultConferenceUser> participants;

    private LayoutInflater inflater;

    private Map<String, RoomPosition> positionMap;

    private Map<String, MediaStream> mediaStreamMap;

    private class RoomPosition {
        double angle;
        double distance;

        RoomPosition(double angle, double distance) {
            this.angle = angle;
            this.distance = distance;
        }
    }

    public ParticipantAdapter(Context context) {
        this.context = context;

        this.mediaStreamMap = new HashMap<>();

        this.participants = new ArrayList<>();

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.positionMap = new HashMap<>();
    }

    public void updateMediaStreams(Map<String, MediaStream> mediaStreamMap) {
        this.mediaStreamMap = mediaStreamMap;
    }

    public void removeParticipant(DefaultConferenceUser user) {
        participants.remove(user);
    }

    private DefaultConferenceUser doesContain(final DefaultConferenceUser user) {
        return Iterables.find(participants, new Predicate<DefaultConferenceUser>() {
            @Override
            public boolean apply(DefaultConferenceUser input) {
                return user.getUserId().equalsIgnoreCase(input.getUserId());
            }
        }, null);
    }

    public void addParticipant(DefaultConferenceUser conferenceUser) {
        if (doesContain(conferenceUser) == null) {
            participants.add(conferenceUser);

            positionMap.put(conferenceUser.getUserId(), new RoomPosition(0, 0.5));
        }
    }

    @Override
    public int getCount() {
        return participants.size();
    }

    @Override
    public Object getItem(int position) {
        return participants.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.participants_cell, parent, false);

            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final DefaultConferenceUser user = (DefaultConferenceUser) getItem(position);

        holder.device.setText(user.getDeviceType());

        holder.position.setText(context.getResources().getString(R.string.participant_number, (position + 1)));

        UserInfo info = user.getUserInfo();

        if (mediaStreamMap != null && mediaStreamMap.containsKey(user.getUserId())) {
            MediaStream mediaStream = mediaStreamMap.get(user.getUserId());
            if (mediaStream != null) {
                if (mediaStream.hasVideo()) {
                    holder.avatar.setVisibility(View.VISIBLE);
                    holder.avatar.attach(user.getUserId(), mediaStreamMap.get(user.getUserId()));
                } else {
                    holder.avatar.setVisibility(View.GONE);
                    holder.avatar.unAttach();
                }
            }
        }

        if (info != null && info.getName() != null && info.getName().length() > 0)
            holder.userId.setText(info.getName());
        else
            holder.userId.setText(user.getUserId());

        Picasso.with(context).load(info.getAvatarUrl()).into(holder.avatarImage);
        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoxeetSdk.getInstance().getConferenceService().muteUser(user.getUserId(), !user.isMuted());

                Toast.makeText(context, "Mute set to: " + user.isMuted(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.angle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                updatePosition(user.getUserId(), progress, holder.distance.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        holder.distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                updatePosition(user.getUserId(), holder.angle.getProgress(), progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return convertView;
    }

    private void updatePosition(String userId, int x, int y) {
        // angle has to be between -1 and 1
        double angle = ((double) x / 100.0) - 1.0;

        // distance has to be between 0 and 1
        double distance = (double) y / 100.0;

        positionMap.put(userId, new RoomPosition(angle, distance));

        VoxeetSdk.getInstance().getConferenceService().changePeerPosition(userId, angle, distance);
    }

    private class ViewHolder {
        TextView userId;

        TextView device;

        TextView position;

        SeekBar angle;

        SeekBar distance;

        VideoView avatar;

        RoundedImageView avatarImage;

        ViewHolder(View convertView) {
            userId = (TextView) convertView.findViewById(R.id.user_id);

            device = (TextView) convertView.findViewById(R.id.device);

            position = (TextView) convertView.findViewById(R.id.position);

            angle = (SeekBar) convertView.findViewById(R.id.angle);

            distance = (SeekBar) convertView.findViewById(R.id.distance);

            avatar = (VideoView) convertView.findViewById(R.id.avatar);

            avatarImage = (RoundedImageView) convertView.findViewById(R.id.avatar_image);
        }
    }
}
