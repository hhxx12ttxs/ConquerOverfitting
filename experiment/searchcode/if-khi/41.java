package thesis.travelspeakbook.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Item;
import model.MySentence;
import model.MySentenceDecorator;
import model.PositionTag;
import model.supporter.MarkTag;
import model.supporter.MarkTagComparator;
import thesis.travelspeakbook.R;
import thesis.travelspeakbook.current_data.Constant;
import thesis.travelspeakbook.custom_view.FavoriteIconImageButton;
import android.app.Activity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SentenceAdapter extends ArrayAdapter {
	Activity mContext;
	List<MySentence> mSentences;
	String mTextSearch;

	static final String openTagHighLight = "{";
	static final String closeTagHighLight = "}";
	static final String headHighlight = "<b>";
	static final String tailHighlight = "</b>";

	static int NUM_MAX_OUTPUT_DEFAULT = 10;
	static int NUM_MAX_OUTPUT = NUM_MAX_OUTPUT_DEFAULT;
	
	public SentenceAdapter(Activity context, List<MySentence> sentences,
			String textSearch) {
		super(context, R.layout.one_row, sentences);
		this.mContext = context;
		this.mSentences = sentences;
		this.mTextSearch = textSearch;
	}
	

	String highlightTextForVietnamese(String string, MySentence sentence) {
		String[] words = string.split(" ");
		List<String> wordsSigned = new ArrayList<String>();
		String sentenceOutput = sentence.getmText()[Constant
				.getLANGMODE_INPUT().value];
		for (int i = 0; i < words.length; i++) {
			int start = sentence.getmText()[Constant.getLANGMODE_SEARCH().value]
					.toLowerCase().indexOf(words[i].toLowerCase());
			int end = start + words[i].length();

			if (start < 0)
				continue;
			wordsSigned.add(sentenceOutput.substring(start, end));
//			mts.add(new MarkTag(start, openTagHighLight));
//			mts.add(new MarkTag(end, closeTagHighLight));
			
		}
		for (int i = 0; i < wordsSigned.size(); i++) {
			sentenceOutput = sentenceOutput.replace(wordsSigned.get(i),
					openTagHighLight + wordsSigned.get(i) + closeTagHighLight);
		}
		sentenceOutput = sentenceOutput
				.replace(openTagHighLight, headHighlight);
		sentenceOutput = sentenceOutput.replace(closeTagHighLight,
				tailHighlight);

		return sentenceOutput;
	}
	void takePosTagAlready(MySentence sentence, List<MarkTag> mts) {
		if (sentence.getmPattern() == null) {
			return;
		}

		int valueLang = Constant.getLANGMODE_INPUT().value;
		// get mark tag for position
		List<PositionTag> posTags = sentence.getmPattern()
				.getmSemiPatterns()[valueLang].getmPosTags();
		List<Item> tagChooseSelectedItem = sentence.getmTagChooseSelectedItems();
		for (int i = 0; i < posTags.size(); i++) {
			PositionTag pt = posTags.get(i);
			String tag = tagChooseSelectedItem.get(i).getmText()[valueLang];
			mts.add(new MarkTag(pt.getmPos(), Constant.HEADER_HTML));
			mts.add(new MarkTag(pt.getmPos() + tag.length(), Constant.TAIL_HTML));
		}

		List<PositionTag> posTagsManualFill = sentence.getmPattern()
				.getmSemiPatterns()[valueLang].getmPosTagsManualFill();
		
		for (int i = 0; i<posTagsManualFill.size(); i++) {
			PositionTag pt = posTagsManualFill.get(i);
			String tag = sentence.getmTagOptionalSelectedItems().get(i).getmText()[valueLang];
			mts.add(new MarkTag(pt.getmPos(), Constant.HEADER_HTML));
			mts.add(new MarkTag(pt.getmPos() + tag.length(), Constant.TAIL_HTML));
		}
	}
	String getStringHighLightBoth(MySentence s) {
		String textSentence = s.getmText()[Constant.getLANGMODE_INPUT().value];
		if (mTextSearch.equals(""))
			return textSentence;
		mTextSearch = Constant.pre_processing_input(mTextSearch);
		try {
		List<MarkTag> mts = new ArrayList<MarkTag>();
		takePosTagAlready(s, mts);
		
		if (s instanceof MySentenceDecorator) {
			mts.addAll(((MySentenceDecorator)s).getmMarkTags());
		}
		
		String result = "";
		int start = 0;
		int end;
		// TODO: sort mark tag
		Collections.sort(mts, new MarkTagComparator());
		
		for (MarkTag mt : mts) {
			end = mt.getmPos();
			String head = textSentence.substring(start, end);
			result += head + mt.getmTagSign();
			start = end;
		}
		if (start < textSentence.length())
			result += textSentence.substring(start);
		return result;
		} catch (Exception e) {
			Log.d("test", "lỗi câu : " + s.getmText()[Constant.getLANGMODE_INPUT().value]);
		}
//		return "lỗi câu : " + s.getmText()[Constant.getLANGMODE_INPUT().value];
		return "" + s.getmText()[Constant.getLANGMODE_INPUT().value];
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		View row;
		if (convertView == null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row = inflater.inflate(R.layout.one_row, null);
		} else {
			row = (View) convertView;
		}
		
		TextView tvSentence = (TextView) row
				.findViewById(R.id.textViewSentence);
//		final TextView tvCount = (TextView) row
//				.findViewById(R.id.textViewCount);
		final MySentence sentence = mSentences.get(position);
		final FavoriteIconImageButton fiibFavoriteButton = (FavoriteIconImageButton) row.findViewById(R.id.customFavoriteButton);
		fiibFavoriteButton.setmSentence(sentence);
		
		fiibFavoriteButton.setFocusable(false);
		
		// chinh cho tag optional
		// if khong co tag

		// neu co tag thi show mau~ cau cho nhanh

		// version 1: highlight cho text search
		// String text = highlightTextForVietnamese(textSearch, sentence);
//		String textShow = getStringHighlightByTagChoose(sentence,
//				Constant.getLANGMODE_INPUT().value);
		String textShow = getStringHighLightBoth(sentence);

		// version 2: highlight cho pattern
		tvSentence.setText(Html.fromHtml(textShow));
//		tvCount.setText(String.valueOf(sentence.getmCountSelected()) + " - " + sentence.getmPercentMatch() + "%");
	
		return row;
	}
	String upperCaseFirstLetter(String input) {
		// TODO : if empty string ==> out
		if (input.length() == 0) 
			return "";
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}
	String getStringHighlightByTagChoose(MySentence sentence, int valueLang) {

		if (sentence.getmPattern() == null) {
			return sentence.getmText()[valueLang];
		}

		String result = "";

		// TODO: xoa' dong try nay di khi ra san pham
		try {
			String textPattern = sentence.getmPattern().getmText()[valueLang];
			List<PositionTag> posTags = sentence.getmPattern()
					.getmSemiPatterns()[valueLang].getmPosTags();
			List<Item> tagChooseSelectedItem = sentence.getmTagChooseSelectedItems();
			result = textPattern;
			for (int i = 0; i < posTags.size(); i++) {
				PositionTag pt = posTags.get(i);
				String sign = pt.getmTag().getmSign();
				String tag = tagChooseSelectedItem.get(i).getmText()[valueLang];
				if (pt.getmPos() == 0) {
					tag = upperCaseFirstLetter(tag);
				}
				result = result.replace(sign,
						Constant.decorTextTagInput(tag));
			}

			List<PositionTag> posTagsManualFill = sentence.getmPattern()
					.getmSemiPatterns()[valueLang].getmPosTagsManualFill();
			
			for (int i = 0; i<posTagsManualFill.size(); i++) {
				PositionTag pt = posTagsManualFill.get(i);
				String tag = sentence.getmTagOptionalSelectedItems().get(i).getmText()[valueLang];
				String sign = pt.getmTag().getmSign();
				if (pt.getmPos() == 0) {
					tag = upperCaseFirstLetter(tag);
				}
				result = result.replace(sign,
						Constant.decorTextTagOptinal(tag));
			}

		} catch (Exception e) {
			Log.d("test", "co van de : " + sentence.getmText()[valueLang]);
		}

		return result;
	}

}
