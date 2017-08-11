package ug.karuhanga.planimeter;/*
    ObjViewer -- viewer for .obj files -- mainline.

    Partial credits to Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

*/
//TODO Previous Activity Not Getting Destroyed on new creation


import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class Simulate extends AppCompatActivity
  {
    java.util.Map<android.view.MenuItem, Runnable> OptionsMenu;

    ObjectView TheObjectView;

    private int[] CURRENT_OBJECT= new int[2];

    final java.security.SecureRandom Random = new java.security.SecureRandom();
      /* why use anything less */

    float Rand()
      /* returns a random float in [0.0 .. 1.0). */
      {
        final byte[] V = new byte[4];
        Random.nextBytes(V);
        return
                (float)(
                    ((int)V[0] & 255)
                |
                    ((int)V[1] & 255) << 8
                |
                    ((int)V[2] & 255) << 16
                |
                    ((int)V[3] & 255) << 24
                )
            /
                4294967296.0f;
      } /*Rand*/

  /* request codes, all arbitrarily assigned */
    static final int LoadObjectRequest = 1;

    interface RequestResponseAction /* response to an activity result */
      {
        public void Run
          (
            int ResultCode,
            android.content.Intent Data
          );
      } /*RequestResponseAction*/

    java.util.Map<Integer, RequestResponseAction> ActivityResultActions;

    private interface SelectedIDAction
      {
        public void Set
          (
            int SelectedID
          );
      } /*SelectedIDAction*/

    private ObjReader.Model ReadObj
      (
        final int[] file
      )
      {
        ObjReader.Model Result = null;
        try
          {
            Result = ObjReader.ReadObj
              (
                /*FileName =*/ file[0],
                      getApplicationContext(),
                /*LoadMaterials =*/
                    new ObjReader.MaterialLoader()
                      {
                        public ObjReader.MaterialSet Load
                          (
                            ObjReader.MaterialSet Materials,
                            String MatFileName
                          )
                          {
                            return
                                ObjReader.ReadMaterials
                                  (
                                    /*FileName =*/
                                          file[1],
                                          getApplicationContext(),
                                    /*CurMaterials =*/ Materials
                                  );
                          } /*Load*/
                      } /*MaterialLoader*/
              );
          }
        catch (ObjReader.DataFormatException Failed)
          {
            android.widget.Toast.makeText
              (
                /*context =*/ this,
                /*text =*/
                    String.format
                      (
                        GLUseful.StdLocale,
                        getString(R.string.obj_load_fail),
                        Failed.toString()
                      ),
                /*duration =*/ android.widget.Toast.LENGTH_SHORT
              ).show();
          } /*try*/
        return
            Result;
      } /*ReadObj*/


    private class OptionsDialog
        extends android.app.Dialog
        implements android.content.DialogInterface.OnDismissListener
      {
        private final android.content.Context ctx;
        private final String Title;
        private final SelectedIDAction Action;
        private final int InitialButtonID;
        private class ButtonDef
          {
            final String ButtonTitle;
            final int ButtonID;

            public ButtonDef
              (
                String ButtonTitle,
                int ButtonID
              )
              {
                this.ButtonTitle = ButtonTitle;
                this.ButtonID = ButtonID;
              } /*ButtonDef*/
          } /*ButtonDef*/
        private final java.util.ArrayList<ButtonDef> TheButtonDefs =
            new java.util.ArrayList<ButtonDef>();
        private android.widget.RadioGroup TheButtons;

        public OptionsDialog
          (
            android.content.Context ctx,
            String Title,
            SelectedIDAction Action,
            int InitialButtonID
          )
          {
            super(ctx);
            this.ctx = ctx;
            this.Title = Title;
            this.Action = Action;
            this.InitialButtonID = InitialButtonID;
          } /*OptionsDialog*/

        public OptionsDialog AddButton
          (
            String ButtonTitle,
            int ButtonID
          )
          {
            TheButtonDefs.add(new ButtonDef(ButtonTitle, ButtonID));
            return
                this;
          } /*AddButton*/

        @Override
        public void onCreate
          (
            android.os.Bundle ToRestore
          )
          {
            setTitle(Title);
            final android.widget.LinearLayout MainLayout = new android.widget.LinearLayout(ctx);
            MainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
            setContentView(MainLayout);
            TheButtons = new android.widget.RadioGroup(ctx);
            final android.view.ViewGroup.LayoutParams ButtonLayout =
                new android.view.ViewGroup.LayoutParams
                  (
                    android.view.ViewGroup.LayoutParams.FILL_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                  );
            for (ButtonDef ThisButtonDef : TheButtonDefs)
              {
                final android.widget.RadioButton ThisButton =
                    new android.widget.RadioButton(ctx);
                ThisButton.setText(ThisButtonDef.ButtonTitle);
                ThisButton.setId(ThisButtonDef.ButtonID);
                TheButtons.addView(ThisButton, TheButtons.getChildCount(), ButtonLayout);
              } /*for*/
            MainLayout.addView(TheButtons, ButtonLayout);
            TheButtons.check(InitialButtonID);
            setOnDismissListener(this);
          } /*onCreate*/

        @Override
        public void onDismiss
          (
            android.content.DialogInterface TheDialog
          )
          {
            Action.Set(TheButtons.getCheckedRadioButtonId());
          } /*onDismiss*/

      } /*OptionsDialog*/

    @Override
    public boolean onCreateOptionsMenu
      (
        android.view.Menu TheMenu
      )
      {
        OptionsMenu = new java.util.HashMap<android.view.MenuItem, Runnable>();
        OptionsMenu.put
          (
            TheMenu.add(R.string.pick_file),
            new Runnable()
              {
                public void run()
                  {
                    startActivity(new android.content.Intent(android.content.Intent.ACTION_PICK).setClass(Simulate.this, Picker.class).putExtra(Picker.ExtensionID, ".obj").putExtra(Picker.LookInID, new String[]{"Models", "Download",}));
                  } /*run*/
              } /*Runnable*/
          );
        OptionsMenu.put
          (
            TheMenu.add(R.string.random_view),
            new Runnable()
              {
                public void run()
                  {
                    final float
                        X = Rand(),
                        Y = Rand(),
                        Z = Rand(),
                        R = (float)Math.sqrt(X * X + Y * Y + Z * Z),
                        Angle = Rand() * 360.0f;
                    TheObjectView.SetOrientation
                      (
                        new Rotation(Angle, true, X / R, Y / R, Z / R),
                        true
                      );
                  } /*run*/
              } /*Runnable*/
          );
        return super.onCreateOptionsMenu(TheMenu);
      } /*onCreateOptionsMenu*/

    void BuildActivityResultActions()
      {
        ActivityResultActions = new java.util.HashMap<Integer, RequestResponseAction>();
        ActivityResultActions.put
          (
            LoadObjectRequest,
            new RequestResponseAction()
              {
                public void Run
                  (
                    int ResultCode,
                    android.content.Intent Data
                  )
                  {
                  /* Unfortunately I can't send this Intent directly from the ug.karuhanga.planimeter.Picker
                    without using some odd launch-mode settings to avoid another instance
                    of ug.karuhanga.planimeter.Simulate being created. Which is why I do it here. */
                    startActivity
                      (
                        new android.content.Intent(android.content.Intent.ACTION_VIEW, Data.getData())
                            .setClass(Simulate.this, Simulate.class)
                            .setFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                      );
                  } /*Run*/
              } /*RequestResponseAction*/
          );
      } /*BuildActivityResultActions*/

    @Override
    public void onCreate
      (
        android.os.Bundle ToRestore
      )
      {
        super.onCreate(ToRestore);
        setContentView(R.layout.activity_simulate);
        TheObjectView = (ObjectView)findViewById(R.id.object_view);
        BuildActivityResultActions();
        final ObjReader.Model PreviousModel = (ObjReader.Model)getLastNonConfigurationInstance();
        if (PreviousModel != null)
          {
            TheObjectView.SetObject(PreviousModel);
          } /*if*/
        if (ToRestore != null)
          {
          /* reload previously-viewed object */
            int[] savedObject = ToRestore.getIntArray(getString(R.string.name_objects_intent));
            if (PreviousModel == null && savedObject != null)
              {
                TheObjectView.SetObject(ReadObj(savedObject));
                CURRENT_OBJECT= savedObject;
              } /*if*/
            TheObjectView.onRestoreInstanceState(ToRestore.getParcelable("ug.karuhanga.planimeter.ObjectView"));
              /* doesn't seem to be done by GLSurfaceView */
          }
        else
          {
            android.widget.Toast.makeText
              (
                /*context =*/ this,
                /*text =*/ getString(R.string.startup_prompt),
                /*duration =*/ android.widget.Toast.LENGTH_SHORT
              ).show();
          } /*if*/
        onNewIntent(getIntent());
      } /*onCreate*/

    @Override
    protected void onNewIntent
      (
        android.content.Intent TheIntent
      )
      {
        setIntent(TheIntent);
        String Action = TheIntent.getAction();
        if (Action != null)
          {
            Action = Action.intern();
          } /*if*/
        if (Action == android.content.Intent.ACTION_VIEW)
          {
            int[] data= new int[2];
            try {
              data = TheIntent.getExtras().getIntArray("objects");
            }catch (NullPointerException e){
              Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
              data= CURRENT_OBJECT;
            }
            final ObjReader.Model NewObj = ReadObj(data);
            if (NewObj != null)
              {
                CURRENT_OBJECT = data;
                TheObjectView.SetObject(NewObj);
              } /*if*/
          } /*if*/
      } /*onnewIntent*/

    @Override
    public void onPause()
      {
        super.onPause();
        TheObjectView.onPause();
      } /*onPause*/

    @Override
    public void onResume()
      {
        super.onResume();
        TheObjectView.onResume();
      } /*onResume*/

//    @Override
//    public Object onRetainNonConfigurationInstance()
//      /* optimization to avoid re-reading .obj file */
//      {
//        return
//            TheObjectView.GetObject();
//      } /*onRetainNonConfigurationInstance*/

    @Override
    public void onSaveInstanceState
      (
        android.os.Bundle ToRestore
      )
      {
        if (CURRENT_OBJECT != null)
          {
          /* remember what file I was looking at */
            ToRestore.putIntArray(getString(R.string.name_objects_intent), CURRENT_OBJECT);
          } /*if*/
        ToRestore.putParcelable("ug.karuhanga.planimeter.ObjectView", TheObjectView.onSaveInstanceState());
          /* doesn't seem to be done by GLSurfaceView */
      } /*onSaveInstanceState*/

    @Override
    public boolean onOptionsItemSelected
      (
        android.view.MenuItem TheItem
      )
      {
        boolean Handled = false;
        final Runnable Action = OptionsMenu.get(TheItem);
        if (Action != null)
          {
            Action.run();
            Handled = true;
          } /*if*/
        return
            Handled;
      } /*onOptionsItemSelected*/


  } /*ug.karuhanga.planimeter.Simulate*/
