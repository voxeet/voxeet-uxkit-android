package fr.voxeet.sdk.sample.users;

import com.voxeet.sdk.json.UserInfo;
import com.voxeet.sdk.sample.R;

import java.util.ArrayList;
import java.util.List;

import fr.voxeet.sdk.sample.main_screen.UserItem;

public class UsersHelper {

    public final static UserItem[] USER_ITEMS = {
            new UserItem(R.drawable.team_alexis, new UserInfo("Alexis", "6664", "https://cdn.voxeet.com/images/team-alexis.png")),
            new UserItem(R.drawable.team_benoit, new UserInfo("Benoit", "1114", "https://cdn.voxeet.com/images/team-benoit-senard.png")),
            new UserItem(R.drawable.team_barnabe, new UserInfo("Barnabé", "7774", "https://cdn.voxeet.com/images/team-barnabe.png")),
            new UserItem(R.drawable.team_corentin, new UserInfo("Corentin", "8884", "https://cdn.voxeet.com/images/team-corentin.png")),
            new UserItem(R.drawable.team_julie, new UserInfo("Julie", "5554", "https://cdn.voxeet.com/images/team-julie-egglington.png")),
            new UserItem(R.drawable.team_raphael, new UserInfo("Raphael", "4444", "https://cdn.voxeet.com/images/team-raphael.png")),
            new UserItem(R.drawable.team_romain, new UserInfo("Romain", "9994", "https://cdn.voxeet.com/images/team-romain.png")),
            new UserItem(R.drawable.team_stephane, new UserInfo("Stephane", "2224", "https://cdn.voxeet.com/images/team-stephane-giraudie.png")),
            new UserItem(R.drawable.team_thomas, new UserInfo("Thomas", "3334", "https://cdn.voxeet.com/images/team-thomas.png"))
    };

    public static List<UserInfo> getExternalIds(UserItem owner) {
        if (owner.getUserInfo() != null)
            return getExternalIds(owner.getUserInfo().getExternalId());
        return getExternalIds((String) null);
    }

    public static List<UserInfo> getExternalIds(String owner) {
        List<UserInfo> list = new ArrayList<>();

        for (UserItem item : USER_ITEMS) {
            if (item.getUserInfo() != null) {
                if (!item.getUserInfo().getExternalId().equals(owner)) {
                    list.add(item.getUserInfo());
                }
            }
        }

        return list;
    }
}
