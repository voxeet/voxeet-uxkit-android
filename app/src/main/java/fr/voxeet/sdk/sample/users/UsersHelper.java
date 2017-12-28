package fr.voxeet.sdk.sample.users;

import java.util.ArrayList;
import java.util.List;

import fr.voxeet.sdk.sample.R;
import fr.voxeet.sdk.sample.main_screen.UserItem;
import voxeet.com.sdk.json.UserInfo;

/**
 * Created by kevinleperf on 24/11/2017.
 */

public class UsersHelper {

    public final static UserItem[] USER_ITEMS = {
            new UserItem(R.drawable.team_alexis, new UserInfo("Alexis", "666", "https://cdn.voxeet.com/images/team-alexis.png")),
            new UserItem(R.drawable.team_benoit, new UserInfo("Benoit", "111", "https://cdn.voxeet.com/images/team-benoit-senard.png")),
            new UserItem(R.drawable.team_barnabe, new UserInfo("Barnabé", "777", "https://cdn.voxeet.com/images/team-barnabe.png")),
            new UserItem(R.drawable.team_corentin, new UserInfo("Corentin", "888", "https://cdn.voxeet.com/images/team-corentin.png")),
            new UserItem(R.drawable.team_julie, new UserInfo("Julie", "555", "https://cdn.voxeet.com/images/team-julie-egglington.png")),
            new UserItem(R.drawable.team_raphael, new UserInfo("Raphael", "444", "https://cdn.voxeet.com/images/team-raphael.png")),
            new UserItem(R.drawable.team_romain, new UserInfo("Romain", "999", "https://cdn.voxeet.com/images/team-romain.png")),
            new UserItem(R.drawable.team_stephane, new UserInfo("Stephane", "222", "https://cdn.voxeet.com/images/team-stephane-giraudie.png")),
            new UserItem(R.drawable.team_thomas, new UserInfo("Thomas", "333", "https://cdn.voxeet.com/images/team-thomas.png"))
    };

    public static List<String> getExternalIds(UserItem owner) {
        if (owner.getUserInfo() != null)
            return getExternalIds(owner.getUserInfo().getExternalId());
        return getExternalIds((String) null);
    }

    public static List<String> getExternalIds(String owner) {
        List<String> list = new ArrayList<>();

        for (UserItem item : USER_ITEMS) {
            if (item.getUserInfo() != null) {
                if (!item.getUserInfo().getExternalId().equals(owner)) {
                    list.add(item.getUserInfo().getExternalId());
                }
            }
        }

        return list;
    }
}
