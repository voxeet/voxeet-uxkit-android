package fr.voxeet.sdk.sample.users;

import com.voxeet.sdk.json.ParticipantInfo;
import com.voxeet.sdk.sample.R;

import java.util.ArrayList;
import java.util.List;

import fr.voxeet.sdk.sample.main_screen.ParticipantItem;

public class UsersHelper {

    public final static ParticipantItem[] USER_ITEMS = {
            new ParticipantItem(R.drawable.team_alexis, new ParticipantInfo("Alexis", "6664", "https://cdn.voxeet.com/images/team-alexis.png")),
            new ParticipantItem(R.drawable.team_benoit, new ParticipantInfo("Benoit", "1114", "https://cdn.voxeet.com/images/team-benoit-senard.png")),
            new ParticipantItem(R.drawable.team_barnabe, new ParticipantInfo("Barnabé", "7774", "https://cdn.voxeet.com/images/team-barnabe.png")),
            new ParticipantItem(R.drawable.team_corentin, new ParticipantInfo("Corentin", "8884", "https://cdn.voxeet.com/images/team-corentin.png")),
            new ParticipantItem(R.drawable.team_julie, new ParticipantInfo("Julie", "5554", "https://cdn.voxeet.com/images/team-julie-egglington.png")),
            new ParticipantItem(R.drawable.team_raphael, new ParticipantInfo("Raphael", "4444", "https://cdn.voxeet.com/images/team-raphael.png")),
            new ParticipantItem(R.drawable.team_romain, new ParticipantInfo("Romain", "9994", "https://cdn.voxeet.com/images/team-romain.png")),
            new ParticipantItem(R.drawable.team_stephane, new ParticipantInfo("Stephane", "2224", "https://cdn.voxeet.com/images/team-stephane-giraudie.png")),
            new ParticipantItem(R.drawable.team_thomas, new ParticipantInfo("Thomas", "3334", "https://cdn.voxeet.com/images/team-thomas.png"))
    };

    public static List<ParticipantInfo> getExternalIds(ParticipantItem owner) {
        if (owner.getParticipantInfo() != null)
            return getExternalIds(owner.getParticipantInfo().getExternalId());
        return getExternalIds((String) null);
    }

    public static List<ParticipantInfo> getExternalIds(String owner) {
        List<ParticipantInfo> list = new ArrayList<>();

        for (ParticipantItem item : USER_ITEMS) {
            if (item.getParticipantInfo() != null) {
                if (!item.getParticipantInfo().getExternalId().equals(owner)) {
                    list.add(item.getParticipantInfo());
                }
            }
        }

        return list;
    }
}
