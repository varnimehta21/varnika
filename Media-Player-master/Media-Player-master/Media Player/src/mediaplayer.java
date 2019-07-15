import javax.media.*;

import java.awt.*;
import java.awt.event.*;

class MediaPlayer extends Frame implements ActionListener,
                                           ControllerListener,
                                           ItemListener
{
   Player player;
   Component vc, cc;
   boolean first = true, loop = false;

   String currentDirectory;

   MediaPlayer (String title)
   {
      super (title);

      addWindowListener
                (new WindowAdapter ()
                 {
                     public void windowClosing (WindowEvent e)
                     {
                        // User selected close from System menu.
                        // Call dispose to invoke windowClosed.

                        dispose ();
                     }

                     public void windowClosed (WindowEvent e)
                     {
                        if (player != null)
                            player.close ();

                        System.exit (0);
                     }
                 });

      Menu m = new Menu ("File");

      MenuItem mi = new MenuItem ("Open...");
      mi.addActionListener (this);
      m.add (mi);

      m.addSeparator ();

      CheckboxMenuItem cbmi = new CheckboxMenuItem ("Loop", false);
      cbmi.addItemListener (this);
      m.add (cbmi);

      m.addSeparator ();

      mi = new MenuItem ("Exit");
      mi.addActionListener (this);
      m.add (mi);

      MenuBar mb = new MenuBar ();
      mb.add (m);
      setMenuBar (mb);

      setSize (200, 200);

      setVisible (true);
   }

   public void actionPerformed (ActionEvent e)
   {
      if (e.getActionCommand ().equals ("Exit"))
      {
          // Call dispose to invoke windowClosed.

          dispose ();
          return;
      }

      FileDialog fd = new FileDialog (this, "Open File",
                                      FileDialog.LOAD);
      fd.setDirectory (currentDirectory);
      fd.show ();

      // If user cancelled, exit.

      if (fd.getFile () == null)
          return;

      currentDirectory = fd.getDirectory ();

      if (player != null)
          player.close ();

      try
      {
         player = Manager.createPlayer (new MediaLocator
                                           ("file:" +
                                            fd.getDirectory () +
                                            fd.getFile ()));
      }
      catch (java.io.IOException e2)
      {
         System.out.println (e2);
         return;
      }
      catch (NoPlayerException e2)
      {
         System.out.println ("Could not find a player.");
         return;
      }

      if (player == null)
      {
          System.out.println ("Trouble creating a player.");
          return;
      }

      first = false;

      setTitle (fd.getFile ());

      player.addControllerListener (this);
      player.prefetch ();
   }

   public void controllerUpdate (ControllerEvent e)
   {
      // A ControllerClosedEvent is posted when player.close is
      // called.  If there is a visual component, this component must
      // be removed.  Otherwise, this visual component appears
      // blanked out on the screen. (To be consistent, we do the same
      // thing for the control panel component.)
      //
      // Note: This problem occurs when run under JMF 2.1 Windows and
      //       SDK 1.3 on a Windows 98 SE platform.

      if (e instanceof ControllerClosedEvent)
      {
          if (vc != null)
          {
              remove (vc);
              vc = null;
          }

          if (cc != null)
          {
              remove (cc);
              cc = null;
          }

          return;
      }

      if (e instanceof EndOfMediaEvent)
      {
          if (loop)
          {
              player.setMediaTime (new Time (0));
              player.start ();
          }

          return;
      }

      if (e instanceof PrefetchCompleteEvent)
      {
          player.start ();
          return;
      }

      if (e instanceof RealizeCompleteEvent)
      {
          vc = player.getVisualComponent ();
          if (vc != null)
              add (vc);

          cc = player.getControlPanelComponent ();
          if (cc != null)
              add (cc, BorderLayout.SOUTH);

          pack ();
      }
   }

   public void itemStateChanged (ItemEvent e)
   {
      loop = !loop;
   }

   public void paint (Graphics g)
   {
      if (first)
      {
          int w = getSize ().width;
          int h = getSize ().height;

          g.setColor (Color.blue);
          g.fillRect (0, 0, w, h);

          Font f = new Font ("DialogInput", Font.BOLD, 16);
          g.setFont (f);

          FontMetrics fm = g.getFontMetrics ();
          int swidth = fm.stringWidth ("*** Welcome ***");

          g.setColor (Color.white);
          g.drawString ("*** Welcome ***",
                        (w - swidth) / 2,
                        (h + getInsets ().top) / 2);
      }

      // Call overridden Frame superclass paint method.  That method
      // will call each contained container and component (including
      // the control panel component) paint method.

      super.paint (g);
   }

   // Eliminate control panel component flicker by preventing frame
   // background from being cleared.

   public void update (Graphics g)
   {
      paint (g);
   }

   public static void main (String [] args)
   {
      new MediaPlayer ("Media Player 1.0");
   }
}