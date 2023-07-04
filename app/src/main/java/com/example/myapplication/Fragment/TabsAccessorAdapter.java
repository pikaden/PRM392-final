package com.example.myapplication.Fragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.myapplication.Fragment.ChatsFragment;
import com.example.myapplication.Fragment.ContactsFragment;
import com.example.myapplication.Fragment.GroupsFragment;
import com.example.myapplication.Fragment.RequestsFragment;

// TODO: switch to ViewPager2 and androidx.viewpager2.adapter.FragmentStateAdapter
public class TabsAccessorAdapter extends FragmentStatePagerAdapter {
    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    // 4 screen in app when logged in
    @Override
    public Fragment getItem(int i) {

        switch (i)
        {
            case 0:
                ChatsFragment chatsFragment =new ChatsFragment();
                return chatsFragment;

            case 1:
                GroupsFragment groupFragment =new GroupsFragment();
                return groupFragment;

            case 2:
                ContactsFragment contactsFragment =new ContactsFragment();
                return contactsFragment;


            case 3:
                RequestsFragment requestsFragment =new  RequestsFragment();
                return requestsFragment;

            default:
                    return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }
    public CharSequence getPageTitle(int position)
    {



        switch (position)
        {
            case 0:

                return "Chat";

            case 1:

                return "Groups";

            case 2:

                return "Friends";

            case 3:

                return "Requests";

            default:
                return null;
        }




    }
}
