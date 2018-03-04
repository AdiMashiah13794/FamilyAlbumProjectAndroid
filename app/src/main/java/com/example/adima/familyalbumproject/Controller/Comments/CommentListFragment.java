package com.example.adima.familyalbumproject.Controller.Comments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.adima.familyalbumproject.Album.Model.Album;
import com.example.adima.familyalbumproject.Comment.Model.Comment;
import com.example.adima.familyalbumproject.Comment.Model.CommentListViewModel;
import com.example.adima.familyalbumproject.Controller.MainActivity;
import com.example.adima.familyalbumproject.R;

import java.util.LinkedList;
import java.util.List;

import Model.Model;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CommentListFragemnt.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CommentListFragemnt#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentListFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private String albumId;
    List<Comment> commentList = new LinkedList<>();
    CommentListAdapter adapter;
    ProgressBar progressBar;
    private CommentListViewModel commentsListViewModel;

    public CommentListFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EmployeeListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommentListFragment newInstance(String albumId) {
        CommentListFragment fragment = new CommentListFragment();
        Bundle args = new Bundle();
        args.putString("albumId", albumId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_comments, container, false);
        view.findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).showAlbumFragment(albumId);
            }
        });
        ListView list = view.findViewById(R.id.comment_listView);
        adapter = new CommentListAdapter();
        list.setAdapter(adapter);
        progressBar = view.findViewById(R.id.commentList_progressbar);
        progressBar.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            //throw new RuntimeException(context.toString()
            //        + " must implement OnFragmentInteractionListener");
        }
        Bundle args = getArguments();
        albumId = args.getString("albumId", "");

        commentsListViewModel = ViewModelProviders.of(this).get(CommentListViewModel.class);
        commentsListViewModel.init(albumId);
        commentsListViewModel.getCommentsList().observe(this, new Observer<List<Comment>>() {
            @Override
            public void onChanged(@Nullable List<Comment> comments) {
                commentList = comments;
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onItemSelected(Comment comment);
    }

    class CommentListAdapter extends BaseAdapter {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @Override
        public int getCount() {
            return commentList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.cell_comment, null);
            }
            TextView commentText = (TextView) convertView.findViewById(R.id.cell_comment_text);
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.cell_comment_user_image);
            final ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.comment_cell_progressBar);
            final Comment cmt = commentList.get(position);
            commentText.setText(cmt.getText());
            imageView.setTag(cmt.getImageUrl());
            imageView.setImageDrawable(getContext().getDrawable(R.drawable.avatar));
            if (cmt.getImageUrl() != null && !cmt.getImageUrl().isEmpty() && !cmt.getImageUrl().equals("")) {
                progressBar.setVisibility(View.VISIBLE);
                Model.instance().getImage(cmt.getImageUrl(), new Model.GetImageListener() {
                    @Override
                    public void onSuccess(Bitmap image) {
                        String tagUrl = imageView.getTag().toString();
                        if (tagUrl.equals(cmt.getImageUrl())) {
                            imageView.setImageBitmap(image);
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFail() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
            return convertView;
        }
    }
}