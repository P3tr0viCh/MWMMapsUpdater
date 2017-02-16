package ru.p3tr0vich.mwmmapsupdater.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.p3tr0vich.mwmmapsupdater.FragmentMain.OnListFragmentInteractionListener;
import ru.p3tr0vich.mwmmapsupdater.R;
import ru.p3tr0vich.mwmmapsupdater.models.MapItem;

public class MapItemRecyclerViewAdapter extends RecyclerView.Adapter<MapItemRecyclerViewAdapter.ViewHolder> {

    private final List<MapItem> mValues;

    private final OnListFragmentInteractionListener mListener;

    public MapItemRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        super();

        setHasStableIds(true);

        mValues = new ArrayList<>();

        mListener = listener;
    }

    public void swapItems(@Nullable List<MapItem> items) {
        mValues.clear();

        if (items != null) {
            mValues.addAll(items);
        }

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        holder.mTextName.setText(holder.mItem.getName());
        holder.mTextDescription.setText(holder.mItem.getDescription());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return mValues.get(position).getId().hashCode();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;

        private final TextView mTextName;
        private final TextView mTextDescription;

        private MapItem mItem;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            mTextName = (TextView) view.findViewById(R.id.text_name);
            mTextDescription = (TextView) view.findViewById(R.id.text_description);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextName.getText() + "'";
        }
    }
}