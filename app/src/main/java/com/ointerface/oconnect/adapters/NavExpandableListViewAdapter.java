package com.ointerface.oconnect.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ointerface.oconnect.App;
import com.ointerface.oconnect.R;
import com.ointerface.oconnect.activities.OConnectBaseActivity;
import com.ointerface.oconnect.containers.MenuItemHolder;
import com.ointerface.oconnect.data.MasterNotification;
import com.ointerface.oconnect.util.AppUtil;

import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.view.View.GONE;

/**
 * Created by AnthonyDoan on 4/15/17.
 */

public class NavExpandableListViewAdapter extends BaseExpandableListAdapter {
    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<MenuItemHolder>> _listDataChild;

    public View section1Header;

    private TextView tvAlertText;

    public NavExpandableListViewAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<MenuItemHolder>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final MenuItemHolder childItem = (MenuItemHolder) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_view_group_item, null);
        }

        ImageView ivMenuItem = (ImageView) convertView.findViewById(R.id.ivMenuIcon);

        // ivMenuItem.setImageResource(childItem.menuIconResID);

        ivMenuItem.setBackgroundResource(childItem.menuIconResID);

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.tvMenuText);

        txtListChild.setText(childItem.menuText);

        tvAlertText = (TextView) convertView.findViewById(R.id.tvAlertText);

        // tvAlertText.setVisibility(GONE);

        if (childItem.menuIconResID == R.drawable.icon_announcements) {
            setNewAlertTextView();
        } else {
            tvAlertText.setVisibility(GONE);
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_view_group_header, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        TextView tvArrow = (TextView) convertView.findViewById(R.id.tvArrow);

        tvArrow.setText(">");

        if (isExpanded == true) {
            tvArrow.setRotation(90);
        } else {
            tvArrow.setRotation(0);
        }

        // We hide the first header.
        if (groupPosition == 0) {
            tvArrow.setVisibility(GONE);
            convertView.setVisibility(GONE);
            section1Header = convertView;
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setNewAlertTextView () {
        Realm realm = AppUtil.getRealmInstance(App.getInstance());

        RealmResults<MasterNotification> alertResults;

        alertResults  = realm.where(MasterNotification.class).equalTo("conference", AppUtil.getSelectedConferenceID(_context)).equalTo("isNew", true).findAllSorted("createdAt", Sort.DESCENDING);

        RealmList<MasterNotification> tempResults = new RealmList<MasterNotification>();

        tempResults.addAll(alertResults);

        if (OConnectBaseActivity.currentPerson != null) {
            for (int i = tempResults.size() - 1; i >= 0; --i) {
                MasterNotification alert = tempResults.get(i);

                RealmList<MasterNotification> deletedAlerts = OConnectBaseActivity.currentPerson.getDeletedNotificationIds();

                for (int j = deletedAlerts.size() - 1; j >= 0; --j) {
                    MasterNotification thisAlert = deletedAlerts.get(j);
                    if (alert.getObjectId().contentEquals(thisAlert.getObjectId())) {
                        realm.beginTransaction();
                        alertResults.remove(i);
                        realm.commitTransaction();
                    }
                }
            }
        }

        if (alertResults.size() > 0) {
            tvAlertText.setVisibility(View.VISIBLE);
            tvAlertText.setText("" + alertResults.size());
        } else {
            tvAlertText.setVisibility(GONE);
        }
    }
}
