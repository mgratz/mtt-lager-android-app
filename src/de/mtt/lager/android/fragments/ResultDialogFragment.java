package de.mtt.lager.android.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.greenrobot.event.EventBus;
import de.mtt.lager.android.R;
import de.mtt.lager.android.backend.Bus;
import de.mtt.lager.android.database.Article;


public class ResultDialogFragment extends DialogFragment {

	public static final String BUNDLE_ARTICLE_ID = "bundle_article_id";

	public static DialogFragment newInstance(String articleId){
		Bundle bundle = new Bundle();
		bundle.putString(BUNDLE_ARTICLE_ID, articleId);
		ResultDialogFragment fragment = new ResultDialogFragment();
		fragment.setArguments(bundle);
		fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		return fragment;
	}

	private TextView barcode;

	private TextView description;

	private Button okButton;

	private ImageView picture;

	private View resultView;

	private TextView title;

	public ResultDialogFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_result_view, container);
		barcode = (TextView)view.findViewById(R.id.activity_result_view_barcode);
		title = (TextView)view.findViewById(R.id.activity_result_view_title);
		description = (TextView)view.findViewById(R.id.activity_result_view_description);
		picture =(ImageView)view.findViewById(R.id.activity_result_view_picture);
		okButton = (Button)view.findViewById(R.id.activity_result_view_button_ok);
		resultView = view.findViewById(R.id.result_view);
		return view;
	}


	public void onEventMainThread(Bus.Articles.Success result){
		if(result.getData()!=null){
			resultView.setVisibility(View.VISIBLE);
			Article article = result.getData();
			showResult(article);
		}

	}
	@Override
	public void onPause() {
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);
	}


	private void showResult(Article article) {
		barcode.setText(article.getOrderno());
		title.setText(article.getTitle());
		description.setText(article.getDescription());
		Picasso.with(getActivity()).load(article.getPictureURL()).into(picture);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
}
