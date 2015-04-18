import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;

/************************************************************************
 * Author: Drew Kim
 * Date: 9/4/14
 * Block: A
 * Program: Build Your Own Web Browser
 * 
 * Description: A functioning web browser that implements branched history.
 * Features include: forward and back buttons, go button, search engine
 * feature through intelligent url bar.
 ************************************************************************/
public class Browser 
{
	private static JEditorPane editorPane;
	private static JButton goButton = new JButton("Go");
	private static JTextField urlBar = new JTextField("http://en.wikipedia.org/wiki/Main_Page", 75);
	private static JPanel navPanel = new JPanel();
	private static HistoryNode historyHead;
	private static JMenu historyMenu = new JMenu("Forward");
	private static JButton backButton = new JButton("Back");
	private static JMenuBar container = new JMenuBar();
	private static JFrame frame;
	private static JMenuBar menuBar = new JMenuBar();
	private static JMenu history = new JMenu("History");
	private static JMenuItem clearHistoryItem = new JMenuItem("Clear History");

	/**
	 * Class that listens for hyperlinks that have been clicked. Goes to page and
	 * enters the link in the history if clicked.
	 * @author Drew
	 *
	 */
	private static class ActivatedHyperlinkListener implements HyperlinkListener 
	{
		/**
		 * Listens for hyperlinks and visits clicked hyperlinks
		 */
		public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) 
		{
			HyperlinkEvent.EventType type = hyperlinkEvent.getEventType();
			final URL url = hyperlinkEvent.getURL();
			if (type == HyperlinkEvent.EventType.ENTERED) 
			{
				System.out.println("URL: " + url);
			} 
			else if (type == HyperlinkEvent.EventType.ACTIVATED) 
			{
				System.out.println("Activated");

				Document doc = editorPane.getDocument();
				try 
				{
					editorPane.setPage(url);
					urlBar.setText(url.toString());

					setHistoryNodeForward();
				} 
				catch (IOException e1) 
				{
					System.out.println("Error following link, Invalid link");
					editorPane.setDocument(doc);
				}
			}
		}
	}

	/**
	 * Action listener for when the user visits a webpage using the url bar.
	 * Enters link in history if successfully visited, presents user with error message if not.
	 * @author Drew
	 *
	 */
	private static class EnterLinkAction implements ActionListener
	{
		/**
		 * Listens for links entered through url bar
		 */
		public void actionPerformed(ActionEvent e) 
		{
			URL url = null;
			String urlString = urlBar.getText();
			try 
			{	
				url = modifyURLForSearchQuery(url, urlString);

				//Sets the editor pane to the correct webpage and then changes the history node so that it is pointing to the correct entry
				editorPane.setPage(url);
				urlBar.setText(url.toString());

				setHistoryNodeForward();

				fillForwardMenu();
			}
			catch (MalformedURLException e1) 
			{
				JOptionPane.showMessageDialog(frame, "Invalid URL", "Error", JOptionPane.ERROR_MESSAGE);
			} 
			catch (IOException e1) 
			{
				JOptionPane.showMessageDialog(frame, "Invalid URL", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Action listener for when the user clicks the back button. 
	 * Steps back one entry in the history.
	 * @author Drew
	 *
	 */
	private static class BackAction implements ActionListener
	{
		/**
		 * Listens for when user clicks back button
		 */
		public void actionPerformed(ActionEvent e) 
		{
			if(e.getSource().equals(backButton))
			{
				//If the current node has a parent, go to it
				if(historyHead.getParent() != null)
				{
					historyHead = historyHead.getParent();
					try 
					{
						//Set the editor pane to the previous history entry
						editorPane.setPage(new URL(historyHead.getURL()));
						urlBar.setText(historyHead.getURL());

						fillForwardMenu();
					} 
					catch (IOException e1) 
					{
						JOptionPane.showMessageDialog(frame, "Unknown Error", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}

	/**
	 * Action listener for when user clicks the forward button.
	 * Steps forward to selected webpage.
	 * @author Drew
	 *
	 */
	private static class ForwardAction implements ActionListener
	{
		/**
		 * Listens for when user clicks forward button
		 */
		public void actionPerformed(ActionEvent e) 
		{			
			URL url = null;

			//Sets the editor pane to the correct webpage and then moves the history pointer to the correct entry
			try 
			{
				url = new URL(((JMenuItem)e.getSource()).getText());

				historyHead = historyHead.getChildAtIndex(Integer.parseInt(e.getActionCommand()));
				editorPane.setPage(url);
				urlBar.setText(url.toString());

				fillForwardMenu();
			} 
			catch (IOException e1) 
			{
				JOptionPane.showMessageDialog(frame, "Unknown Error", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Action listener for menu items on the menu bar
	 * @author Drew
	 *
	 */
	private static class MenuBarAction implements ActionListener
	{
		/**
		 * Listens for when user clicks menu items
		 */
		public void actionPerformed(ActionEvent e) 
		{
			//Clears the history
			if(e.getSource().equals(clearHistoryItem))
			{
				historyHead = new HistoryNode(urlBar.getText(), null);
				historyMenu.removeAll();
				JOptionPane.showMessageDialog(frame, "History Cleared", "Success", JOptionPane.DEFAULT_OPTION);
			}
		}
	}

	/**
	 * A linked list of history entries. Contains an array of up to 100 children.
	 * @author Drew
	 *
	 */
	private static class HistoryNode
	{
		private String url;
		private HistoryNode[] children;
		private HistoryNode parent;
		private int currentIndex;

		/**
		 * Constructor. Initializes necessary data.
		 * @param url		The url to give the node
		 * @param parent	The parent of the node
		 */
		public HistoryNode(String url, HistoryNode parent)
		{
			children = null;
			this.url = url;
			this.parent = parent;
			currentIndex = 0;
		}

		/**
		 * Returns the url for a specific history entry.
		 * @return
		 */
		public String getURL()
		{
			return url;
		}

		/**
		 * Returns the child at a specific index
		 * @param index		The index of the child being searched for
		 * @return
		 */
		public HistoryNode getChildAtIndex(int index)
		{
			return children[index];
		}

		/**
		 * Adds a child to the history
		 * @param child		The child to add
		 * @return
		 */
		public boolean addChild(HistoryNode child)
		{
			if(children == null)
				children = new HistoryNode[100];

			if(currentIndex + 1 > 99)
			{
				return false;
			}
			else
			{
				children[currentIndex] = child;
				currentIndex++;
				return true;
			}
		}

		/**
		 * Returns the parent of the node
		 * @return
		 */
		public HistoryNode getParent()
		{
			return parent;
		}

		/**
		 * Returns the current child number the node is on
		 * @return
		 */
		public int getCurrentIndex()
		{
			return currentIndex;
		}

		/**
		 * Returns whether the node has children or not
		 * @return
		 */
		public boolean hasChildren()
		{
			if(children == null)
				return false;
			else
				return true;
		}
	}

	/**
	 * Main method. Instantiates all the necessary GUI items, history items, and JPanel. Sets homepage to Wikipedia.
	 * @param args
	 */
	public static void main(String args[]) 
	{
		//Set up the GUI items
		frame = new JFrame("Drew's Browser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		navPanel.setLayout(new FlowLayout());		

		backButton.addActionListener(new BackAction());
		navPanel.add(backButton);

		container.add(historyMenu);
		navPanel.add(container);

		urlBar.addActionListener(new EnterLinkAction());
		navPanel.add(urlBar);

		goButton.addActionListener(new EnterLinkAction());
		navPanel.add(goButton);

		frame.add(navPanel, BorderLayout.NORTH);
				
		clearHistoryItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
		clearHistoryItem.addActionListener(new MenuBarAction());
		
		history.add(clearHistoryItem);
		
		menuBar.add(history);
		
		frame.setJMenuBar(menuBar);

		//Set up hyperlink listener, scroll pane, and go to Wikipedia
		try 
		{
			editorPane = new JEditorPane("http://en.wikipedia.org/wiki/Main_Page");
			editorPane.setEditable(false);

			historyHead = new HistoryNode(editorPane.getPage().toString(), null);
			
			HyperlinkListener hyperlinkListener = new ActivatedHyperlinkListener();
			editorPane.addHyperlinkListener(hyperlinkListener);
			JScrollPane scrollPane = new JScrollPane(editorPane);

			frame.add(scrollPane, BorderLayout.CENTER);
		} 
		catch (IOException e) 
		{
			System.err.println("Unable to load: " + e);
		}

		frame.setSize(1440, 900);
		frame.setVisible(true);
	}

	/**
	 * Checks to see whether a string has a period in it or not.
	 * Used to differentiate between an attempt to visit a webpage or search for something
	 * @param str		The string to check
	 * @return
	 */
	private static boolean hasDot(String str)
	{
		for(int i = 0; i < str.length(); i++)
		{
			if(str.charAt(i) == '.')
				return true;
		}

		return false;
	}

	/**
	 * Fills the forward history menu with correct entries.
	 */
	private static void fillForwardMenu()
	{
		if(historyHead.hasChildren())
		{
			historyMenu.removeAll();
			for(int i = 0; i < historyHead.getCurrentIndex(); i++)
			{
				JMenuItem item = new JMenuItem(historyHead.getChildAtIndex(i).getURL());
				item.addActionListener(new ForwardAction());
				item.setActionCommand("" + i);
				historyMenu.add(item);	
			}
		}
		else
		{
			historyMenu.removeAll();
		}
	}

	/**
	 * Adds a new entry to the history and then sets the pointer to the correct position
	 */
	private static void setHistoryNodeForward()
	{
		historyHead.addChild(new HistoryNode(editorPane.getPage().toString(), historyHead));
		historyHead = historyHead.getChildAtIndex(historyHead.getCurrentIndex() - 1);
	}

	/**
	 * Modifies the url to either a search query or correct url form (http://)
	 * @param url			The url to return
	 * @param urlString		The string verion of the url
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	private static URL modifyURLForSearchQuery(URL url, String urlString) throws MalformedURLException, UnsupportedEncodingException
	{
		//If url entered doesn't have an http://, but is a webpage, add an http://. Otherwise, search for the entered text
		if(hasDot(urlString) && urlString.length() >= 7)
		{
			if(!urlString.substring(0, 7).equals("http://"))
				urlString = "http://" + urlString;
			url = new URL(urlString);
		}
		else if(hasDot(urlString) && urlString.length() < 7)
		{
			url = new URL(urlString);
		}
		else
		{
			url = new URL("http://duckduckgo.com/html/?q=" + URLEncoder.encode(urlString, "UTF-8"));
		}

		return url;
	}
}