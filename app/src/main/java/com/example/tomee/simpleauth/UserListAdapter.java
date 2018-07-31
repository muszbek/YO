package com.example.tomee.simpleauth;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private List<String> dataSet;
    private Context parentContext;
    private String currentUid;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public UserView userView;
        public ViewHolder(UserView user) {
            super(user);
            userView = user;
            userView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    MenuInflater inflater = new MenuInflater(v.getContext());
                    inflater.inflate(R.menu.context_menu, menu);

                    if (userView.getContext() instanceof AddFriendActivity) {
                        MenuItem item = (MenuItem) menu.findItem(R.id.delete_friend);
                        item.setVisible(false);
                    }
                }
            });
        }
    }

    public String getCurrentUid() {
        return currentUid;
    }

    public void setCurrentUid(String newUid) {
        this.currentUid = newUid;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public UserListAdapter(List myDataset) {
        dataSet = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        parentContext = parent.getContext();
        // create a new view
        UserView user = (UserView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_view, parent, false);

        ViewHolder vh = new ViewHolder(user);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.userView.construct(parentContext, dataSet.get(position));

        holder.userView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setCurrentUid(holder.userView.mUid);
                return false;
            }
        });
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.userView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}
