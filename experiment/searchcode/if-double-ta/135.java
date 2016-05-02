package steamgaugeapp.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import steamgaugeapp.client.gwtfb.FBCore;
import steamgaugeapp.client.gwtfb.FBEvent;
import steamgaugeapp.client.gwtfb.JSOModel;
import steamgaugeapp.shared.FieldVerifier;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SteamGauge implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	// private final SteamUserServiceAsync steamUserService =
	// GWT.create(SteamUserService.class);

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);

	private TabPanel tp = new TabPanel();
	private final TextBox nameField = new TextBox();

	private final boolean[] lockEngaged = { true, true, true, true };
	private final boolean[] userLock = { true, true };
	private Label comparingLabel = new Label("Comparing users, please wait.");
	private Label mygamesLabel = new Label("Retrieving additional game info, please wait.");
	
	String[] idArr;
	String[] titleArr;
	int currentUnknownGame;
	boolean autoLoginLock = true;
	boolean nameEntered = false;

	// List of recommended games retrieved from the server
	private ArrayList<Game> recGames = new ArrayList<Game>();

	// List of compared games retrieved from the server
	private ArrayList<Game> compGames = new ArrayList<Game>();

	// List of games that the target user owns
	private ArrayList<Game> targetGames = new ArrayList<Game>();

	// List of games that the target user owns
	private ArrayList<Game> userGames = new ArrayList<Game>();

	// List of review objects to be displayed in reviews tab
	private ArrayList<Review> reviewsList = new ArrayList<Review>();
	
	// arraylist of facebook friends who have the app installed (by fb id)
	//private ArrayList<Long> friendids = new ArrayList<Long>();

	//Compare games
	ToggleButton mutualB;
	ToggleButton multiplayerB;
	VerticalPanel comparePanel;
	
	//My page maps
	private HashMap<HorizontalPanel, Integer> title2id = new HashMap<HorizontalPanel, Integer>();
	private HashMap<Button, Integer> showButton2id = new HashMap<Button, Integer>();
	private HashMap<TextArea, Integer> reviewArea2id = new HashMap<TextArea, Integer>();
	private HashMap<Button, Integer> submitReview2id = new HashMap<Button, Integer>();
	private HashMap<Game, Integer> gameReview2id = new HashMap<Game, Integer>();
	private HashMap<DockPanel, Integer> myDock2id = new HashMap<DockPanel, Integer>();
	//private HashMap<Button, Integer> reviewButton2gid = new HashMap<Button, Integer>();
	private int currentID;
	private HashSet<String> reviewIDs = new HashSet<String>();
	private int current10 = 0;
	Button prev = new Button("<- Prev 10");
	Button next = new Button("Next 10 ->");
	private ClickHandler showRevHandler = new ClickHandler(){
		@Override
		public void onClick(ClickEvent event) {
			Button b = ((Button) event.getSource());

			int id = showButton2id.get(b);

			for (TextArea ta : reviewArea2id.keySet()) {
				if (reviewArea2id.get(ta) == id) {
					if (ta.isVisible()) {
						ta.setVisible(false);
						b.setText("Submit review");
					} else {
						ta.setVisible(true);
						b.setText("Hide");
					}
				}
			}

			for (Button butt : submitReview2id.keySet()) {
				if (submitReview2id.get(butt) == id) {
					if (butt.isVisible()) {
						butt.setVisible(false);
					} else {
						butt.setVisible(true);
					}
				}
			}
		}

	};
	private ClickHandler submitRevHandler = new ClickHandler(){

		@Override
		public void onClick(ClickEvent event) {
			((Button) event.getSource()).setVisible(false);

			int id = submitReview2id.get((Button) event.getSource());

			for (TextArea ta : reviewArea2id.keySet()) {
				if (reviewArea2id.get(ta) == id) {
					Game gameOut = null;
					for (Game g : gameReview2id.keySet()) {
						if (gameReview2id.get(g) == id) {
							gameOut = g;
						}
					}

					currentID = id;
					greetingService.addReview(currentuser.getFbid(),
							gameOut.getId(), ta.getText(),
							new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									System.out.println("Failed to add review");
								}

								@Override
								public void onSuccess(Void result) {
									for(Button b: showButton2id.keySet()){
										if(showButton2id.get(b) == currentID){
											b.setVisible(false);
										}
									}
									
									for(TextArea ta : reviewArea2id.keySet()){
										if(reviewArea2id.get(ta) == currentID){
											ta.setVisible(false);
										}
									}
									
									for(Button b : submitReview2id.keySet()){
										if(submitReview2id.get(b) == currentID){
											b.setVisible(false);
										}
									}
									
									for (DockPanel dp : myDock2id.keySet()) {
										if (myDock2id.get(dp) == currentID) {
											dpToSetText = dp;
											updateCurrentReviews();
										}
									}

								}

							});
				}
			}
			
		}
		
	};

	//Reviews page maps
	private HashMap<Button, Review> upvote2Review = new HashMap<Button, Review>();
	private HashMap<Button, Review> downvote2Review = new HashMap<Button, Review>();
	private HashMap<Review, HTML> review2Score = new HashMap<Review, HTML>();
	private Button currUpDownButton;
	private String reviewsTarget = "";
	private String reviewsTargetPrep = "";
	private ListBox reviewDropDown;
	private HashMap<String, Integer> reviewTitle2id = new HashMap<String, Integer>();
	private TextArea testTA = new TextArea();
	
	// Images
	final Image downvote = new Image("downvote.png");
	final Image upvote_empty = new Image("upvote_empty.png");
	final Image downvote_empty = new Image("downvote_empty.png");

	static final ArrayList<SteamUser> users = new ArrayList<SteamUser>(); // list of all steam users
	//static final ArrayList<SteamUser> friends = new ArrayList<SteamUser>(); // list of all users who are also friends
	private HorizontalPanel horiz = new HorizontalPanel();
	private CellTable<SteamUser> table = new CellTable<SteamUser>();
	// Create name column.
	TextColumn<SteamUser> nameColumn = new TextColumn<SteamUser>() {
		@Override
		public String getValue(SteamUser user) {
			return user.getId();
		}
	};

	final SingleSelectionModel<SteamUser> selectionModel = new SingleSelectionModel<SteamUser>();
	// Create a data provider.
	ListDataProvider<SteamUser> dataProvider = new ListDataProvider<SteamUser>();
	List<SteamUser> tableList = new ArrayList<SteamUser>();

	final Label loadingLabel = new Label("Loading game list...");
	final Label debugLabel = new Label();

	private SteamUser currentuser; // the user who's currently "logged in"
	private SteamUser targetuser; // the user who was clicked on in the
									// cellTable

	private DockPanel dpToSetText;

	// dev version appid
	public String appID = "377089098999023";

	// real version appid
	//public String appID = "364413073604896";

	private FBCore fbCore = GWT.create(FBCore.class);
	private FBEvent fbEvent = GWT.create(FBEvent.class);

	private boolean status = true;
	private boolean xfbml = true;
	private boolean cookie = true;

	private String reviewsText;
	
	private long fbid = 0;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		fbCore.init(appID, status, cookie, xfbml);
		
		//
		// Callback used when session status is changed
		//
		class SessionChangeCallback extends Callback<JavaScriptObject> {
			public void onSuccess ( JavaScriptObject response ) {
			    // Make sure cookie is set so we can use the non async method
			    renderHomeView();
			}
		}
		
		//
		// Get notified when user session is changed
		//
		SessionChangeCallback sessionChangeCallback = new SessionChangeCallback ();
		fbEvent.subscribe("auth.statusChange",sessionChangeCallback);
		
		final Button sendButton = new Button("Send");

		nameField.setText("Custom URL");

		final Label errorLabel = new Label();

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);

		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();

		// Create the popup dialog box
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("Remote Procedure Call");
		dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		prev.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				next.setVisible(true);
				current10 = current10 - 10;
				if(current10 == 0){
					prev.setVisible(false);
				}
				
				Widget reviewPage = tp.getWidget(4);
				tp.remove(3);
				tp.remove(3);
				
				tp.add(setupMyPage(), "My Games");
				tp.add(reviewPage, "Reviews");
				tp.selectTab(3);
			}
			
		});
		
		next.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				prev.setVisible(true);
				current10 = current10 + 10;
				if(userGames.size() - current10 < 10){
					next.setVisible(false);
				}
				
				Widget reviewPage = tp.getWidget(4);
				tp.remove(3);
				tp.remove(3);
				
				tp.add(setupMyPage(), "My Games");
				tp.add(reviewPage, "Reviews");
				tp.selectTab(3);
			}
			
		});
		prev.setVisible(false);
		
		addTabPanel();
		getUsers();
		refreshPersonalPages();

		
		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				sendButton.setEnabled(true);
				sendButton.setFocus(true);
			}
		});

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendNameToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendNameToServer();
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a
			 * response.
			 */
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel.setText("");
				final String textToServer = nameField.getText();
				if (!FieldVerifier.isValidCustomUrl(textToServer)) {
					errorLabel.setText("You entered an invalid custom URL.");
					return;
				}

				// Then, we send the input to the server.
				sendButton.setEnabled(false);
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");
				RootPanel.get("errorLabelContainer").add(loadingLabel);
				greetingService.greetServer(textToServer+"<TOKEN>"+fbid,
						new AsyncCallback<String[]>() {
							public void onFailure(Throwable caught) {
								System.out.println("GreetServer failure");
							}

							public void onSuccess(String[] result) {
								nameEntered = true;
								currentuser = getSteamUserFromString(result[1]);
								//getUserGames();
								users.add(currentuser);
								System.out.println("AUTOLOGIN 2");
								handleUnknownGames(result[2], result[3]);
								handleReviews(result[4]);
								handleReviewTitles(result[5]);
								
								parseUserGames(result[6]);
								targetuser = null;
							}
						});
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
		
	}

	public void renderHomeView() {
		if(fbCore.getAuthResponse() == null){
			renderWhenNotLoggedIn();
		}
		else renderWhenLoggedIn();
	}

	private void renderWhenLoggedIn() {
		if(fbid!=0) return;
		fbCore.api("/me", new AsyncCallback<JavaScriptObject>(){

			@Override
			public void onFailure(Throwable caught) {
				System.out.println("failed to get facebook id for logged-in user");
			}

			@Override
			public void onSuccess(JavaScriptObject result) {
				JSOModel jso = result.cast();
				String id = jso.get("id");
				//System.out.println(id);
				fbid = Long.parseLong(id);
				//getFbFriends();
				try {
					greetingService.getUser(fbid, new AsyncCallback<String>(){
						@Override
						public void onFailure(Throwable caught) {
							System.out.println("welp");
							RootPanel.get("enterurl").setStyleName("showEl");
							RootPanel.get("tabContainer").setVisible(false);
							RootPanel.get("loading").setStyleName("hideEl");
						}

						@Override
						public void onSuccess(String result) {
							
							
							currentuser = getSteamUserFromString(result);
							
							RootPanel.get("title").setStyleName("showEl");
							RootPanel.get("enterurl").setStyleName("hideEl");
							//RootPanel.get("tabContainer").setVisible(true);
							
							if(autoLoginLock == true){
								autoLoginLock = false;
								greetingService.greetServer(currentuser.getId()+"<TOKEN>"+fbid,
										new AsyncCallback<String[]>() {
											public void onFailure(Throwable caught) {
												
												System.out.println("FAILURE AUTOLOGIN");
											}

											public void onSuccess(String[] result) {
												currentuser = getSteamUserFromString(result[1]);
												//getUserGames();
												System.out.println("AUTOLOGIN 1");
												userLock[1] = false;
												handleUnknownGames(result[2], result[3]);
												handleReviews(result[4]);
												handleReviewTitles(result[5]);
												
												parseUserGames(result[6]);
												targetuser = null;
												//RootPanel.get("loading").setStyleName("hideEl");
											}
										});
							}
						}
						
					});
				} catch (ServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		RootPanel.get("title").setStyleName("showEl");
		RootPanel.get("logged out").setStyleName("hideEl");
		
	}

	/*protected void getFbFriends() {
		if(friends.size()>0) return;
		fbCore.api("/me/friends", new AsyncCallback<JavaScriptObject>(){

			@Override
			public void onFailure(Throwable caught) {
				System.out.println("Could not get list of friends.");
			}

			@Override
			public void onSuccess(JavaScriptObject result) {
		        JSOModel jso = result.cast ();
		        if ( jso.hasKey ( "error" ) ) System.out.println("JSO Error");
		        else {
			        JsArray<JSOModel> array = jso.getArray("data");
				       
			        for ( int i = 0 ; i < array.length(); i++ ) {
			            JSOModel j = array.get(i).cast();
			            String id = j.get("id");
			            System.out.println("friend id "+id);
			            friendids.add(new Long(Long.parseLong(id)));
			        }
		        }
			}
		});
	}*/

	private void renderWhenNotLoggedIn() {
		RootPanel.get("title").setStyleName("hideEl");
		RootPanel.get("enterurl").setStyleName("hideEl");
		RootPanel.get("tabContainer").setVisible(false);
		RootPanel.get("logged out").setStyleName("showEl");
		fbid = 0;
		autoLoginLock = true;
	}

	
	private void handleReviewTitles(String result){
		String[] reviews = result.split("<REVIEW>");
		reviewDropDown = new ListBox();
		reviewTitle2id.clear();
		
		reviewDropDown.addItem("All Titles");
		reviewTitle2id.put("All Titles", -1);
		
		for(String review : reviews){
			
			String[] rParts = review.split("<TOKEN>");
			if(rParts[0].equals("")) continue;
			
			reviewIDs.clear();
			
			reviewIDs.add(rParts[0]);
			
			reviewDropDown.addItem(rParts[1]);
			
			reviewTitle2id.put(rParts[1], Integer.parseInt(rParts[0]));
		}
		
		reviewDropDown.addChangeHandler(new ChangeHandler(){
			@Override
			public void onChange(ChangeEvent event) {
				String title = reviewDropDown.getItemText(reviewDropDown.getSelectedIndex());
				testTA.setText(testTA.getText() + title + "\n");
				int gid = reviewTitle2id.get(title);
				testTA.setText(testTA.getText() + gid + "\n");
				reviewsTargetPrep = title;
				
				if(gid != -1){
					greetingService.getReviews(gid, currentuser.getFbid(), new AsyncCallback<String>(){
						
						@Override
						public void onFailure(Throwable caught) {
								System.out.println("unsucessfully found reviews for gid ");
										
						}
			
						@Override
						public void onSuccess(String result) {
							testTA.setText(testTA.getText() + result + "\n");
							reviewsTarget = reviewsTargetPrep;
							handleReviews(result);
							tp.remove(4);
							tp.add(setupReviewPage(), "Reviews");
							tp.selectTab(4);
						}
									
						});
				}
				else{
					greetingService.getReviews(currentuser.getFbid(), new AsyncCallback<String>(){
						
						@Override
						public void onFailure(Throwable caught) {
								System.out.println("unsucessfully found reviews for gid ");
										
						}
			
						@Override
						public void onSuccess(String result) {
							reviewsTarget = "";
							handleReviews(result);
							tp.remove(4);
							tp.add(setupReviewPage(), "Reviews");
							tp.selectTab(4);
						}
									
					});
				}
			}
		});
		tp.remove(4);
		tp.add(setupReviewPage(), "Reviews");
		tp.selectTab(4);
	}

	private void handleReviews(String reviewsString) {
		String[] reviews = reviewsString.split("<REVIEW>");

		reviewsList.clear();
		if(!reviewsString.isEmpty()){
			for (String review : reviews) {
				String[] content = review.split("<TOKEN>");
	
				if (!content[1].equals("")) {
					long id = Long.parseLong(content[0]);
					int gid = Integer.parseInt(content[1]);
					long fbid = Long.parseLong(content[2]);
					String date = content[3];
					String text = content[4];
					int upvotes = Integer.parseInt(content[5]);
					int downvotes = Integer.parseInt(content[6]);
					Boolean hasUpvoted = null;
					if (content[7].equals("true")) {
						hasUpvoted = true;
					} else {
						hasUpvoted = false;
					}
					Boolean hasDownvoted = null;
					if (content[8].equals("true")) {
						hasDownvoted = true;
					} else {
						hasDownvoted = false;
					}
	
					reviewsList.add(new Review(id, gid, fbid, date, text,
							hasUpvoted, hasDownvoted, upvotes, downvotes));
				}
			}
		}
		/*
		 * lockEngaged[3] = false; checkLocks();
		 */
	}
	
	public void parseUserGames(String gamesString){
		userGames.clear();
		String[] games = gamesString.split("<GAME>");
		
		for(String game : games){
			Game g = constructGameObject(game);
			if (g != null)
				userGames.add(g);
		}
		
		userLock[0] = false;
		checkUserLocks();
	}

	private void handleUnknownGames(String idResults, String titleResults) {
		idArr = idResults.split("<TOKEN>");
		titleArr = titleResults.split("<TOKEN>");
		currentUnknownGame = 0;
		if (!idResults.equals("")) {
			recursiveAddGames(currentUnknownGame);
		} else {
			userLock[1] = false;
			checkUserLocks();
		}
	}

	private void recursiveAddGames(int gameNumber) {

		if (gameNumber < idArr.length) {
			greetingService.addRemoteGame(Integer.parseInt(idArr[gameNumber]),
					titleArr[gameNumber], new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							System.out.println("OH  NO");
						}

						@Override
						public void onSuccess(String result) {
							currentUnknownGame++;
							recursiveAddGames(currentUnknownGame);
							String percentage = (currentUnknownGame / (double) idArr.length)
									+ "";
							if (percentage.length() > 2) {
								percentage = percentage.substring(2);
								percentage += "00";
							}
							if (percentage.length() > 2) {
								percentage = percentage.substring(0, 2);
							}
							if(percentage.equals("00")){
								loadingLabel.setText("100% done adding games to database...");
							}
							else{
								loadingLabel.setText(percentage + "% done adding games to database...");
							}
							System.out.println("% " + percentage);
						}

					});
		} else {
			RootPanel.get("errorLabelContainer").remove(loadingLabel);
			refreshPersonalPages();
			refreshCompareGames();
			userLock[1] = false;
			checkUserLocks();
		}
	}

	public int[] getRecommendations(String HTML) {
		String searchPhrase = "recommendation_cap_0";

		int index = HTML.indexOf(searchPhrase);

		ArrayList<Integer> listOut = new ArrayList<Integer>();

		while (index != -1 && index > 0 && index < HTML.length()) {
			HTML = HTML.substring(index, HTML.length());

			if (retrieveID(HTML) != -1) {
				listOut.add(retrieveID(HTML));
			}

			HTML = HTML.substring(searchPhrase.length(), HTML.length());
			index = HTML.indexOf(searchPhrase);
		}

		int[] arrOut = new int[listOut.size()];

		for (int i = 0; i < listOut.size(); i++) {
			arrOut[i] = listOut.get(i);
		}

		return arrOut;
	}

	public int retrieveID(String HTML) {
		String searchFor = "http://store.steampowered.com/app/";
		int index = HTML.indexOf(searchFor) + searchFor.length();

		HTML = HTML.substring(index);

		try {
			int out = Integer.parseInt(HTML.substring(0, HTML.indexOf("/")));
			return out;
		} catch (NumberFormatException e) {
			return -1;
		}

	}

	/**
	 * 
	 * @return the list of all steam users in the datastore
	 */
	private void getUsers() {
		greetingService.getUsers(new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				SteamGauge.users.clear();
				//SteamGauge.friends.clear();
				ArrayList<SteamUser> ulist = getSteamUsersFromString(result);
				for(SteamUser u : ulist){
					if(u==null) continue;
					/*if(friendids.contains(new Long(u.getFbid()))){
						SteamGauge.friends.add(u);
					}*/
					SteamGauge.users.add(u);
				}
				refreshUserList();
				if(!autoLoginLock){
					RootPanel.get("tabContainer").setVisible(true);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				System.out.println("get users failure");
			}
		});
	}

	private Game constructGameObject(String result) {
		String[] arr = result.split("<TOKEN>");

		int id = -1;
		String name = null;
		boolean multiplayer = false;
		int[] recommendations = null;

		int i = 0;
		for (String s : arr) {
			if (s.equals(""))
				return null;
			if (i == 0) {
				id = Integer.parseInt(s);
			} else if (i == 1) {
				name = s;
			} else if (i == 2) {
				if (s.equals("false")) {
					multiplayer = false;
				} else {
					multiplayer = true;
				}
			} else {
				String[] recs = s.split("<RECS>");
				recommendations = new int[recs.length];

				for (int j = 0; j < recs.length; j++) {
					recommendations[j] = Integer.parseInt(recs[j]);
				}
			}

			i++;
		}

		Game g = new Game();
		g.id = id;
		g.title = name;
		g.multiplayer = multiplayer;
		g.recs = recommendations;

		return g;
	}

	private ArrayList<SteamUser> getSteamUsersFromString(String s) {
		ArrayList<SteamUser> result = new ArrayList<SteamUser>();
		String[] temp = s.split("<TOKEN1>");
		ArrayList<String> stringusers = new ArrayList<String>(
				Arrays.asList(temp));
		for (String u : stringusers) {
			result.add(getSteamUserFromString(u));
		}
		return result;
	}

	protected SteamUser getSteamUserFromString(String s) {
		if (s.equals(""))
			return null;
		String[] clean = s.split("<TOKEN1>");
		s = clean[0];
		String[] fields = s.split("<TOKEN2>");
		String[] gamelist = fields[3].split("<TOKEN3>");
		ArrayList<String> gamelistarray = new ArrayList<String>(
				Arrays.asList(gamelist));
		ArrayList<OwnedGame> games = new ArrayList<OwnedGame>();
		for (String g : gamelistarray) {
			String[] gamelistvalues = g.split("<TOKEN4>");
			games.add(new OwnedGame(Integer.parseInt(gamelistvalues[0]), Double
					.parseDouble(gamelistvalues[1])));
		}
		return new SteamUser(Integer.parseInt(fields[0]), fields[1], fields[2], games);
	}
	
	protected void refreshUserList() {
		if(currentuser == null) return;
		tableList.clear();
		/*for(SteamUser user : friends){
			if (!(user.getId().equals(currentuser.getId()))) tableList.add(user);
		}*/
		for (SteamUser user : users) {
			if (!(user.getId().equals(currentuser.getId()))) tableList.add(user);
		}
	}
	
	private void refreshPersonalPages(){
		int currIndex = tp.getTabBar().getSelectedTab();
		tp.remove(3);
		tp.remove(3);
		tp.add(setupMyPage(), "My Games");
		tp.add(setupReviewPage(), "Reviews");
		tp.selectTab(currIndex);
		if(nameEntered){
			RootPanel.get("enterurl").setVisible(false);
			RootPanel.get("tabContainer").setVisible(true);
		}
		
	}
	
	private void refreshCompareGames(){
		int currIndex = tp.getTabBar().getSelectedTab();
		Widget recPage = tp.getWidget(2);
		Widget myGames = tp.getWidget(3);
		Widget reviews = tp.getWidget(4);
		tp.remove(1);
		tp.remove(1);
		tp.remove(1);
		tp.add(setupComparePage(), "Compare Games");
		tp.add(recPage, "Recommended");
		tp.add(myGames, "My Games");
		tp.add(reviews, "Reviews");
		tp.selectTab(currIndex);
	}
	
	private void refreshTargetPages(){
		int currIndex = tp.getTabBar().getSelectedTab();
		Widget w = tp.getWidget(3);
		Widget e = tp.getWidget(4);
		tp.remove(0);
		tp.remove(0);
		tp.remove(0);
		tp.remove(0);
		tp.remove(0);
		tp.add(setupFriendsPage(), "Friend's Games");
		tp.add(setupComparePage(), "Compare Games");
		tp.add(setupRecPage(), "Recommended");
		tp.add(w, "My Games");
		tp.add(e, "Reviews");
		tp.selectTab(currIndex);
	}

	protected void refreshTabPanel() {	
		System.out.println("refreshing");
		RootPanel.get("loading").setStyleName("showEl");
		RootPanel.get("loading").setVisible(true);
		System.out.println(RootPanel.get("loading").getStyleName());
		int currIndex = tp.getTabBar().getSelectedTab();
		tp.remove(0);
		tp.remove(0);
		tp.remove(0);
		tp.remove(0);
		tp.remove(0);
		
		tp.add(setupFriendsPage(), "Friend's Games");
		tp.add(setupComparePage(), "Compare Games");
		tp.add(setupRecPage(), "Recommended");
		tp.add(setupMyPage(), "My Games");
		tp.add(setupReviewPage(), "Reviews");
		tp.selectTab(currIndex);

		// Add the data to the data provider, which automatically pushes it to
		// the
		// widget.
		tableList.clear();
		/*for(SteamUser user : friends){
			if (!(user.getId().equals(currentuser.getId()))) tableList.add(user);
		}*/
		for (SteamUser user : users) {
			if (!(user.getId().equals(currentuser.getId()))) tableList.add(user);
		}

		if (currentuser != null){
			RootPanel.get("tabContainer").setVisible(true);
			RootPanel.get("enterurl").setStyleName("hideEl");
		}
		System.out.println("done refreshing");	
		RootPanel.get("loading").setStyleName("hideEl");
	}

	private void addTabPanel() {
		RootPanel.get("loading").setStyleName("showEl");
		// table = the cellTable

		// Create name column.
		TextColumn<SteamUser> nameColumn = new TextColumn<SteamUser>() {
			@Override
			public String getValue(SteamUser user) {
				return user.getName();
			}
		};

		// Create a data provider.
		ListDataProvider<SteamUser> dataProvider = new ListDataProvider<SteamUser>();

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Add the data to the data provider, which automatically pushes it to
		// the
		// widget.
		tableList = dataProvider.getList();
		/*for(SteamUser user : friends){
			if (!(user.getId().equals(currentuser.getId()))) tableList.add(user);
		}*/
		for (SteamUser user : users) {
			if (!(user.getId().equals(currentuser.getId()))) tableList.add(user);
		}

		table.addColumn(nameColumn, "Friends");
		table.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						SteamUser selected = selectionModel.getSelectedObject();
						if (selected != null) {
							lockEngaged[0] = true;
							lockEngaged[1] = true;
							lockEngaged[2] = true;
							RootPanel.get("errorLabelContainer").add(
									comparingLabel);
							targetuser = selected;
							recGames.clear();
							compGames.clear();
							targetGames.clear();
							
							String gids = "";
							for (int i = 0; i < targetuser.getGameList().size(); i++) {
								OwnedGame og = targetuser.getGameList().get(i);													
								gids = gids + og.getId() + "<TOKEN>";														
							}
							getTargetGames(gids);
							
							// get recommendations
							getRecommendations();
							// get compared games
							getSharedGames();
						}

					}

				});

		horiz.add(table);
		horiz.add(tp);

		tp.add(setupFriendsPage(), "Friend's Games");
		tp.add(setupComparePage(), "Compare Games");
		tp.add(setupRecPage(), "Recommended");
		tp.add(setupMyPage(), "My Games");
		tp.add(setupReviewPage(), "Reviews");

		tp.setSize("800px", "300px");
		tp.selectTab(1);

		// Add it to the root panel.
		//ERIC, GETTING EXCEPTION HERE FOR SOME REASON
		/**/
		//RootPanel.get("tabContainer").remove(0);
		RootPanel.get("tabContainer").setVisible(false);
		RootPanel.get("tabContainer").add(horiz);
		RootPanel.get("loading").setStyleName("hideEl");
	}
	
	private void getTargetGames(String gids){
		greetingService.getGames(gids, new AsyncCallback<String>(){

			@Override
			public void onFailure(
					Throwable caught) {
				System.out.println("Failure to get game");
			}

			@Override
			public void onSuccess(String result) {
				String[] games = result.split("<GAME>");
				
				//System.out.println("SHOULD BE SPLITTING HERE " + games.length);
				for(String game : games){
					Game g = constructGameObject(game);
					if (g != null)
						targetGames.add(g);
				}

				lockEngaged[0] = false;
				checkLocks();
			}
			
		});
	}

	private void getRecommendations(){
		recGames.clear();
		greetingService.getRecs(currentuser.getFbid(),
				targetuser.getFbid(),
				new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						System.out.println("Failed to get recommendations");
					}

					@Override
					public void onSuccess(String result) {
						if (!result.equals("")) {
							String[] gamestringarr = result.split("<GAMETOKEN>");
							ArrayList<String> gamestrings = new ArrayList<String>(
									Arrays.asList(gamestringarr));
							ArrayList<Game> tempgamelist = new ArrayList<Game>();
							for (String s : gamestrings) {
								tempgamelist
										.add(constructGameObject(s));
							}

							recGames.addAll(tempgamelist);
							lockEngaged[1] = false;
							checkLocks();
						}
					}
				});	
	}
	
	private void getSharedGames(){
		compGames.clear();
		greetingService.getSharedGames(currentuser.getFbid(),
				targetuser.getFbid(),
				new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						System.out.println("Failed to compare games");
					}

					@Override
					public void onSuccess(String result) {
						ArrayList<Game> tempgamelist = new ArrayList<Game>();
						if (!result.equals("")) {
							String[] gamestringarr = result
									.split("<GAMETOKEN>");
							ArrayList<String> gamestrings = new ArrayList<String>(
									Arrays.asList(gamestringarr));
							for (String s : gamestrings) {
								tempgamelist
										.add(constructGameObject(s));
							}
							compGames.addAll(tempgamelist);
						}
						lockEngaged[2] = false;
						checkLocks();
					}

				});
	}
	
	protected void checkLocks() {
		if (!(lockEngaged[0] || lockEngaged[1] || lockEngaged[2])) {
			RootPanel.get("errorLabelContainer").remove(comparingLabel);
			getUsers();
			refreshTargetPages();
		}
	}

	protected void checkUserLocks() {
		if (!(userLock[0] || userLock[1])) {
			RootPanel.get("errorLabelContainer").remove(loadingLabel);
			//RootPanel.get("instructions").setVisible(false);
			getUsers();
			//refreshMyGames();
			refreshPersonalPages();
			RootPanel.get("enterurl").setVisible(false);
			RootPanel.get("tabContainer").setVisible(true);
		}
	}

	private HTML setupFriendsPage() {
		System.out.println("setting up the friends games page now");
		RootPanel.get("loading").setStyleName("showEl");
		String html = "";

		if (targetuser != null) {
			html = (html + "<h2> " + targetuser.getName() + "'s Games</h2><br/><br/><table>");
			html = (html + "<tr><th>Title</th><th>Hours Played</th></tr>");
			for (OwnedGame og : targetuser.getGameList()) {
				html = (html + "<tr>");

				for (Game g : targetGames) {
					if (g.id == og.getId()) {
						html = (html
								+ "<td><a href='http://store.steampowered.com/app/"
								+ g.getId() + "/'>" + g.getTitle()
								+ "</a></td><td>" + og.getHoursPlayed() + "</td>");
					}
				}

				html = (html + "</tr>");
			}
			html = (html + "</table> ");
		}
		else{
			html = "<p>Select a steamgauge user in the lefthand column to see their games, compare profiles, and recieve recommendations.</p>";
		}

		HTML htmlOUT = new HTML();

		htmlOUT.setHTML(html);

		if (targetGames.size() < 10) {
			htmlOUT.setSize("500px", "600px");
		} else {
			htmlOUT.setWidth("500px");
		}
		
		RootPanel.get("loading").setStyleName("hideEl");

		return htmlOUT;
	}

	// Compare games page html
	private Widget setupComparePage() {
		RootPanel.get("loading").setStyleName("showEl");
		VerticalPanel vp = new VerticalPanel();
		
		System.out.println("setting up the compare page now");
		HTML titleHtml = new HTML();
		HorizontalPanel hp = new HorizontalPanel();
		String html = "";

		if (targetuser != null && currentuser != null) {
			titleHtml = new HTML("<h2>Compare Games</h2><br/>");
			
			mutualB = new ToggleButton("All Mutual Games");
			mutualB.setDown(true);
			mutualB.setEnabled(false);
			
			mutualB.addClickHandler(new ClickHandler(){

				@Override
				public void onClick(ClickEvent event) {
					if(mutualB.isDown()){
						multiplayerB.setEnabled(true);
						multiplayerB.setDown(false);
						mutualB.setEnabled(false);
						
						refreshCompareTable();
					}
				}
				
			});
			
			multiplayerB = new ToggleButton("Mutual Multiplayer Games");
			multiplayerB.addClickHandler(new ClickHandler(){

				@Override
				public void onClick(ClickEvent event) {
					if(multiplayerB.isDown()){
						mutualB.setEnabled(true);
						mutualB.setDown(false);
						multiplayerB.setEnabled(false);
						
						refreshCompareTable();
					}
				}
				
			});
			
			hp.add(mutualB);
			hp.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;"));
			hp.add(multiplayerB);
			
			
		}
		else{
			html = "<p>Select a steamgauge user in the lefthand column to see their games, compare profiles, and recieve recommendations.</p>";
		}

		
		

		vp.add(titleHtml);
		vp.add(hp);
		vp.add(new HTML());
		comparePanel = vp;
		refreshCompareTable();
		
		RootPanel.get("loading").setStyleName("hideEl");
		
		return vp;
	}
	
	private void refreshCompareTable(){
		String html = "";
		
		if (targetuser != null && currentuser != null) {
			
			html = (html + "<br/><table width='500' borde='0'><tr><th>Title</th><th>My Hours</th><th>"
					+ targetuser.getName() + "'s hours</th></tr><br/>");
	
			for (Game g : compGames) {
				if(mutualB.isDown() || (multiplayerB.isDown() && g.isMultiplayer())){
					html = (html
							+ "<tr><td><a href='http://store.steampowered.com/app/"
							+ g.getId() + "/'>" + g.getTitle() + "</a></td>");
		
					for (OwnedGame og : currentuser.getGameList()) {
						if (og.getId() == g.getId()) {
							html = (html + "<td>" + og.getHoursPlayed() + "</td>");
						}
					}
		
					for (OwnedGame og : targetuser.getGameList()) {
						if (og.getId() == g.getId()) {
							html = (html + "<td>" + og.getHoursPlayed() + "</td>");
						}
					}
		
					html = (html + "</tr>");
				}
			}
	
			html = (html + "</table>");
		}
		else{
			html = "<p>Select a steamgauge user in the lefthand column to see their games, compare profiles, and recieve recommendations.</p>";
		}
		
		HTML compareTable = new HTML();
		compareTable.setSize("500px", "600px");
		compareTable.setHTML(html);
		
		
		comparePanel.remove(comparePanel.getWidgetCount() - 1);
		comparePanel.add(compareTable);
	}

	// Recommendations page html
	private HTML setupRecPage() {
		RootPanel.get("loading").setStyleName("showEl");
		System.out.println("setting up the recs page now");
		HTML html = new HTML();

		if (targetuser != null && currentuser != null) {
			html.setHTML("<h2>Recommended games for both players</h2>");
			for (int i = 0; i < recGames.size(); i++) {
				// Need to get image for first item
				ArrayList<String> basedOn = getBasedOn(recGames.get(i).id);

				String basedString = "Based on shared games: ";
				if (basedOn.size() > 0) {
					for (int j = 0; j < 3; j++) {
						if (basedOn.size() > j) {
							basedString = basedString + basedOn.get(j) + ", ";
						}
					}
					if (basedString.length() > 0)
						basedString = basedString.substring(0,
								basedString.length() - 2);
				} else {
					basedString = "";
				}

				if (i == 0) {
					html.setHTML(html
							+ "<h3>1.&nbsp;&nbsp;&nbsp;"
							+ "<a href='http://store.steampowered.com/app/"
							+ recGames.get(0).getId()
							+ "/'>"
							+ "<img src='http://cdn.steampowered.com/v/gfx/apps/"
							+ recGames.get(0).id
							+ "/header_292x136.jpg' alt='img' /></a><br/>"
							+ "&nbsp;&nbsp;&nbsp;<b><a href='http://store.steampowered.com/app/"
							+ recGames.get(0).getId() + "/'>"
							+ recGames.get(0).title + "</a></b><br/>"
							+ "&nbsp;&nbsp; " + basedString);

					html.setHTML(html + "</h3><br/>");
				} else {
					html.setHTML(html
							+ "<h4>"
							+ (i + 1)
							+ " .&nbsp;&nbsp;&nbsp;<b><a href='http://store.steampowered.com/app/"
							+ recGames.get(i).getId() + "/'>"
							+ recGames.get(i).getTitle()
							+ "</a></b><br/>&nbsp;&nbsp;&nbsp;" + basedString);

					html.setHTML(html + "</h4><br/>");
				}
			}
		}
		else{
			html.setHTML("<p>Select a steamgauge user in the lefthand column to see their games," +
					" compare profiles, and recieve recommendations.</p>");
		}

		if (recGames.size() < 10) {
			html.setSize("500px", "600px");
		} else {
			html.setWidth("500px");
		}

		RootPanel.get("loading").setStyleName("hideEl");
		
		return html;
	}

	private Widget setupMyPage() {
		RootPanel.get("loading").setStyleName("showEl");
		System.out.println("setting up the my games page now");
		VerticalPanel vp = new VerticalPanel();
		
		// Button for updating games list
		Button updateGamesList = new Button("Update My Games List");
		updateGamesList.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				try {
					greetingService.updateGamesList(currentuser.getFbid(),
							new AsyncCallback<String[]>(){

						@Override
						public void onFailure(Throwable caught) {
							System.out.println("Failed to update uid.");
						}

						@Override
						public void onSuccess(String[] result) {
							// need to re-get the current user
							currentuser = getSteamUserFromString(result[0]);
							parseUserGames(result[1]);
						}
					});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		// Submit button (and textbox) for changing the current user's uid
		final TextBox uidbox = new TextBox();
		uidbox.setText("Change your custom URL");
		Button submitUidChange = new Button("Submit");
		submitUidChange.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event){
				try {
					greetingService.updateUser(currentuser.getFbid(), uidbox.getText(), new AsyncCallback<String[]>(){

						@Override
						public void onFailure(Throwable caught) {
							System.out.println("Failed to update uid.");
							uidbox.setText("Failed to find user ID...");
						}

						@Override
						public void onSuccess(String[] result) {							
							if(users.size() > 0){
								for(SteamUser u : ((ArrayList<SteamUser>)users.clone())){
									if(u != null){
										if(u.getFbid() == currentuser.getFbid()){
											users.remove(u);
										}
									}
								}
							}
							
							
							currentuser = getSteamUserFromString(result[0]);
							users.add(currentuser);
							
							parseUserGames(result[1]);
							
							for(Review r : reviewsList){
								r.refreshUsername();
							}	
							
							handleUnknownGames(result[2], result[3]);
							
							tp.remove(4);
							tp.add(setupReviewPage(), "Reviews");
							
							lockEngaged[0] = false;
							lockEngaged[1] = true;
							lockEngaged[2] = true;
							if(targetuser != null){
								getRecommendations();
								getSharedGames();
							}
							// need to re-get the current user
						}
					});
				} catch (ServerException e) {
					System.out.println("Server exception while changing uid: "+e);
				}
				uidbox.setText("Loading user profile...");
			}
		});

		HorizontalPanel topHoriz = new HorizontalPanel();
		HTML mySpacing = new HTML("");
		mySpacing.setSize("350px", "20px");
		updateGamesList.setSize("200px", "30px");
		topHoriz.add(updateGamesList);
		HTML anotherSpacing = new HTML("");
		anotherSpacing.setSize("100px", "30px");
		topHoriz.add(mySpacing);
		topHoriz.add(uidbox);
		topHoriz.add(submitUidChange);
		vp.add(topHoriz);
		

		HTML verticalSpace = new HTML("");
		verticalSpace.setSize("400px", "30px");
		vp.add(verticalSpace);
		
		if(currentuser != null){
			vp.add(new HTML("<h2>" + currentuser.getName() + "'s Games</h2>"));
		}
		
		title2id.clear();

		showButton2id.clear();
		reviewArea2id.clear();
		submitReview2id.clear();
		gameReview2id.clear();
		myDock2id.clear();

		for (int i = current10; i < current10 + 10; i++) {
			if(userGames.size() > i){
				Game g = userGames.get(i);
				OwnedGame owned = null;
	
				gameReview2id.put(g, g.getId());
	
				DockPanel dp = new DockPanel();
				myDock2id.put(dp, g.getId());
	
				HTML title = new HTML();
				HTML hours = new HTML();
	
				for (OwnedGame og : currentuser.getGameList()) {
					if (og.getId() == g.getId()) {
						owned = og;
					}
				}
	
				title.setHTML("<html><b>" + g.getTitle() + "</b></html>");
				hours.setHTML("<html>Hours: " + owned.getHoursPlayed()
						+ "</html>");
				title.setWidth("200px");
				hours.setWidth("100px");
				
	
				HorizontalPanel fp = new HorizontalPanel();
				fp.add(title);
				fp.add(hours);
				
				
	
				dp.add(fp, DockPanel.NORTH);
				title2id.put(fp, g.getId());
	
				TextArea ta = new TextArea();
				ta.setSize("400px", "200px");
				ta.setVisible(false);
				reviewArea2id.put(ta, g.getId());
				dp.add(ta, DockPanel.WEST);
	
				Button showButton = new Button("Submit review");
				showButton2id.put(showButton, g.getId());
				showButton.addClickHandler(showRevHandler);
				dp.add(showButton, DockPanel.SOUTH);
	
				Button submitButton = new Button("Submit");
				submitButton.setVisible(false);
				submitReview2id.put(submitButton, g.getId());
				submitButton.addClickHandler(submitRevHandler);
				dp.add(submitButton, DockPanel.CENTER);
	
				vp.add(dp);
				vp.add(new HTML("<br/><br/><br/>"));
			}
		}
		
		
		HTML spacing = new HTML("");
		spacing.setSize("400px", "40px");
		
		HorizontalPanel prevNext = new HorizontalPanel();
		
		prevNext.add(prev);
		prevNext.add(spacing);
		prevNext.add(next);
		
		
		vp.add(prevNext);
		
		RootPanel.get("loading").setStyleName("hideEl");
		
		return vp;
	}

	private void updateCurrentReviews() {
		greetingService.getReviews(currentuser.getFbid(),
				new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						System.out.println("Failed to compare games");
						dpToSetText.add(new HTML(
								"<html>Review unsucessful...<br/></html>"),
								DockPanel.EAST);
					}

					@Override
					public void onSuccess(String result) {
						
						int reviewSizeOld = reviewsList.size();
								
						handleReviews(result);
						
						if(reviewsList.size() == reviewSizeOld){
							updateCurrentReviews();
						}else{
							dpToSetText.add(new HTML("<html>Review added!<br/></html>"),DockPanel.EAST);
							
							reviewIDs.add(myDock2id.get(dpToSetText) + "");
							
							for(Game g : userGames){
								if(g.getId() == myDock2id.get(dpToSetText)){
									boolean alreadyInside = false;
									for(int i = 0; i < reviewDropDown.getItemCount(); i++){
										if(reviewDropDown.getItemText(i).equals(g.getTitle())){
											alreadyInside = true;
										}
									}
									
									if(!alreadyInside){
										reviewDropDown.addItem(g.getTitle());
										reviewTitle2id.put(g.getTitle(), g.getId());
									}
								}
							}
							
							tp.remove(4);
							tp.add(setupReviewPage(), "Reviews");
						}
					}

				});

	}

	private Widget setupReviewPage() {
		RootPanel.get("loading").setStyleName("showEl");
		System.out.println("setting up the review page now");
		VerticalPanel vp = new VerticalPanel();
		
		//******
		testTA.setSize("300px", "300px");
		//vp.add(testTA);
		
		HorizontalPanel hp = new HorizontalPanel();
		
		if(!reviewsTarget.isEmpty()){
			Button allReviews = new Button("All Reviews");
			allReviews.addClickHandler(new ClickHandler(){
				
				@Override
				public void onClick(ClickEvent event) {
								
					greetingService.getReviews(currentuser.getFbid(), new AsyncCallback<String>(){
	
						@Override
						public void onFailure(Throwable caught) {
								System.out.println("unsucessfully found reviews for gid ");
										
						}
			
						@Override
						public void onSuccess(String result) {
							reviewsTarget = "";
							handleReviews(result);
							tp.remove(4);
							tp.add(setupReviewPage(), "Reviews");
							tp.selectTab(4);
							reviewDropDown.setSelectedIndex(0);
						}
									
					});
				}
					
			});
			
			HTML specificHtml = new HTML("<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Currently displaying reviews for " + reviewsTarget + ".</p>");
			
			hp.add(new HTML("&nbsp;&nbsp;&nbsp;"));
			hp.add(allReviews);
			hp.add(specificHtml);
		}
		else{
			HTML allHtml = new HTML("<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Currently displaying all recent reviews.</p>");
			
			hp.add(allHtml);
		}
		
		vp.add(hp);
		
		if(reviewDropDown != null){
			HorizontalPanel dropContainer = new HorizontalPanel();
			dropContainer.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
			dropContainer.add(reviewDropDown);
			vp.add(dropContainer);
		}

		upvote2Review.clear();
		downvote2Review.clear();
		review2Score.clear();
		
		for (int i = 0; i < reviewsList.size(); i++) {
			DockPanel dp = new DockPanel();

			HTML html = new HTML();
			html.setHTML(html + "<html>");

			Review r = reviewsList.get(i);
			html.setHTML(html
					+ "<p><a href='http://store.steampowered.com/app/" + r.gid
					+ "/'>"
					+ "<img src='http://cdn.steampowered.com/v/gfx/apps/"
					+ r.gid + "/header_292x136.jpg' alt='img' /></a><p>");
			dp.add(html, DockPanel.NORTH);

			HTML content = new HTML();
			content.setHTML(content + "<p><i>\"" + r.text + "\"</i></p> - "
					+ r.username);

			content.setHTML(content + "</html>");

			dp.add(content, DockPanel.CENTER);
			
			HTML votes = getVotesHTML(r.upvotes, r.downvotes);

			dp.add(votes, DockPanel.SOUTH);
			review2Score.put(r, votes);

			VerticalPanel upDown = new VerticalPanel();
			
			
			//Upvote
			Button upvote = new Button();
			if(r.hasUpvoted){
				upvote.setHTML("<img border='0' src='upvote.png' />");
			}
			else{
				upvote.setHTML("<img border='0' src='upvote_empty.png' />");
			}
			upvote2Review.put(upvote, r);
			upvote.addClickHandler(new ClickHandler(){

				@Override
				public void onClick(ClickEvent event) {
					currUpDownButton = (Button) event.getSource();
					greetingService.vote(currentuser.getFbid(), upvote2Review.get(event.getSource()).rid, true,
							new AsyncCallback<Void>(){

								@Override
								public void onFailure(Throwable caught) {
									System.out.println("fail upvote");
								}

								@Override
								public void onSuccess(Void result) {
									Review r = upvote2Review.get(currUpDownButton);
									
									if(!r.hasUpvoted && !r.hasDownvoted){
										//Just change to upvote
										r.hasUpvoted = true;
										r.upvotes++;
										
										currUpDownButton.setHTML("<img border='0' src='upvote.png' />");
									}
									else if(r.hasUpvoted){
										r.hasUpvoted = false;
										r.upvotes--;
										
										currUpDownButton.setHTML("<img border='0' src='upvote_empty.png' />");
									}
									else if(r.hasDownvoted){
										r.hasUpvoted = true;
										r.hasDownvoted = false;
										
										r.upvotes++;
										r.downvotes--;
										
										currUpDownButton.setHTML("<img border='0' src='upvote.png' />");
										//REMOVE DOWNVOTE ICON
										for(Button b : downvote2Review.keySet()){
											if(downvote2Review.get(b) == r){
												b.setHTML("<img border='0' src='downvote_empty.png' />");
											}
										}
									}
									
									review2Score.get(r).setHTML(getVotesHTML(r.upvotes, r.downvotes).getHTML());
								}
						
					});
				}
			
			});
			
			//Downvote
			Button downvote = new Button();
			if(r.hasDownvoted){
				downvote.setHTML("<img border='0' src='downvote.png' />");
			}
			else{
				downvote.setHTML("<img border='0' src='downvote_empty.png' />");
			}
			downvote2Review.put(downvote, r);
			downvote.addClickHandler(new ClickHandler(){

				@Override
				public void onClick(ClickEvent event) {
					currUpDownButton = (Button) event.getSource();
					greetingService.vote(currentuser.getFbid(), downvote2Review.get(event.getSource()).rid, false, new AsyncCallback<Void>(){

						@Override
						public void onFailure(Throwable caught) {
							System.out.println("fail downvote");
						}

						@Override
						public void onSuccess(Void result) {
							Review r = downvote2Review.get(currUpDownButton);
							
							if(!r.hasUpvoted && !r.hasDownvoted){
								//Just change to upvote
								r.hasDownvoted = true;
								r.downvotes++;
								
								currUpDownButton.setHTML("<img border='0' src='downvote.png' />");
							}
							else if(r.hasDownvoted){
								r.hasDownvoted = false;
								r.downvotes--;
								
								currUpDownButton.setHTML("<img border='0' src='downvote_empty.png' />");
							}
							else if(r.hasUpvoted){
								r.hasDownvoted = true;
								r.hasUpvoted = false;
								
								r.downvotes++;
								r.upvotes--;
								
								currUpDownButton.setHTML("<img border='0' src='downvote.png' />");
								//REMOVE DOWNVOTE ICON
								for(Button b : upvote2Review.keySet()){
									if(upvote2Review.get(b) == r){
										b.setHTML("<img border='0' src='upvote_empty.png' />");
									}
								}
							}
							
							review2Score.get(r).setHTML(getVotesHTML(r.upvotes, r.downvotes).getHTML());
						}
				
				});
		}
	
	});

			
			upDown.add(upvote);
			upDown.add(downvote);

			dp.add(upDown, DockPanel.WEST);

			vp.add(dp);
		}
		
		RootPanel.get("loading").setStyleName("hideEl");

		return vp;
	}
	
	private HTML getVotesHTML(int upvotes, int downvotes){
		String votes = "<html><p> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		if(upvotes > 0){
			votes = votes + "<font color='orange'>" + upvotes + "</font> / ";
		}else{
			votes= votes + "" + upvotes + " / ";
		}
		
		if(downvotes > 0){
			votes = votes + "<font color='blue'>" + downvotes + "</font> ";
		}else{
			votes = votes + "" + downvotes;
		}
		
		votes = votes + "</p><br/></html>";
		
		HTML htmlOut = new HTML();
		
		htmlOut.setHTML(votes);
		
		return htmlOut;
	}

	private ArrayList<String> getBasedOn(int gid) {
		ArrayList<String> out = new ArrayList<String>();

		for (Game g : compGames) {
			if (g.getRecs() != null) {
				for (int recID : g.getRecs()) {
					if (recID == gid) {
						out.add(g.title);
					}
				}
			}
		}

		return out;
	}

	private class Review {
		public long rid;
		public int gid;
		public long fbid;
		public String date;
		public String text;
		public boolean hasUpvoted;
		public boolean hasDownvoted;
		public int upvotes;
		public int downvotes;
		public String username;

		public Review(long id, int gid, long fbid2, String date, String text,
				boolean hasUpvoted, boolean hasDownvoted, int upvotes,
				int downvotes) {
			this.rid = id;
			this.gid = gid;
			this.fbid=fbid2;
			this.date = date;
			this.text = text;
			this.hasUpvoted = hasUpvoted;
			this.hasDownvoted = hasDownvoted;
			this.upvotes = upvotes;
			this.downvotes = downvotes;

			// make call to get username
			for (SteamUser su : users) {
				if (su.getFbid() == fbid2) {
					username = su.getName();
				}
			}
			/*for (SteamUser su : friends) {
				if (su.getFbid() == fbid2) {
					username = su.getName();
				}
			}*/
		}
		
		public void refreshUsername(){
			for (SteamUser su : users) {

				if (su.getFbid() == fbid) {
					username = su.getName();
				}
			}
		}
	}
}

