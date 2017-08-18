package ug.karuhanga.planimeter;/*
    let the user choose a file to load

    Copyright 2011-2014 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
*/

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class Picker extends android.app.Activity
  {
  /* required extra information passed in launch intent: */
    public static final String LookInID = "ug.karuhanga.planimeter.Picker.LookIn";
      /* array of strings representing names of directories in which to look for files */
      /* ones not beginning with “/” are interpreted as subdirectories in external storage */
    public static final String ExtensionID = "ug.karuhanga.planimeter.Picker.Extension";
      /* extension of filenames to show */

    android.widget.ListView PickerListView;
    SelectedItemAdapter PickerList;

    static class PickerItem
      {
        int[] resIDs= new int[2];
        String name;
        boolean Selected;

        public PickerItem
          (
            int[] resIDs, String name
          )
          {
            this.resIDs = resIDs;
            this.name= name;
            this.Selected = false;
          } /*PickerItem*/

        public String toString()
          /* returns the display name for the item. I use
            the unqualified filename. */
          {
            return name;
          } /*toString*/

      } /*PickerItem*/

    class SelectedItemAdapter extends android.widget.ArrayAdapter<PickerItem>
      {
        final int ResID;
        final android.view.LayoutInflater TemplateInflater;
        PickerItem CurSelected;
        android.widget.RadioButton LastChecked;

        class OnSetCheck implements View.OnClickListener
          {
            final PickerItem MyItem;

            public OnSetCheck
              (
                PickerItem TheItem
              )
              {
                MyItem = TheItem;
              } /*OnSetCheck*/

            public void onClick
              (
                View TheView
              )
              {
                if (MyItem != CurSelected)
                  {
                  /* only allow one item to be selected at a time */
                    if (CurSelected != null)
                      {
                        CurSelected.Selected = false;
                        LastChecked.setChecked(false);
                      } /*if*/
                    LastChecked =
                        TheView instanceof android.widget.RadioButton ?
                            (android.widget.RadioButton)TheView
                        :
                            (android.widget.RadioButton)
                            ((android.view.ViewGroup)TheView).findViewById(R.id.file_item_checked);
                    CurSelected = MyItem;
                    MyItem.Selected = true;
                    LastChecked.setChecked(true);
                  } /*if*/
              } /*onClick*/
          } /*OnSetCheck*/

        SelectedItemAdapter
          (
            android.content.Context TheContext,
            int ResID,
            android.view.LayoutInflater TemplateInflater
          )
          {
            super(TheContext, ResID);
            this.ResID = ResID;
            this.TemplateInflater = TemplateInflater;
            CurSelected = null;
            LastChecked = null;
          } /*SelectedItemAdapter*/

        @Override
        public View getView
          (
            int Position,
            View ReuseView,
            android.view.ViewGroup Parent
          )
          {
            View TheView = ReuseView;
            if (TheView == null)
              {
                TheView = TemplateInflater.inflate(ResID, null);
              } /*if*/
            final PickerItem ThisItem = this.getItem(Position);
            ((android.widget.TextView)TheView.findViewById(R.id.select_file_name))
                .setText(ThisItem.toString());
            final android.widget.RadioButton ThisChecked =
                (android.widget.RadioButton)TheView.findViewById(R.id.file_item_checked);
            ThisChecked.setChecked(ThisItem.Selected);
            final OnSetCheck ThisSetCheck = new OnSetCheck(ThisItem);
            ThisChecked.setOnClickListener(ThisSetCheck);
              /* otherwise radio button can get checked but I don't notice */
            TheView.setOnClickListener(ThisSetCheck);
            return
                TheView;
          } /*getView*/

      } /*SelectedItemAdapter*/

    @Override
    public void onCreate
      (
        android.os.Bundle ToRestore
      )
      {
        super.onCreate(ToRestore);
        setContentView(R.layout.picker);
        PickerList = new SelectedItemAdapter(this, R.layout.picker_item, getLayoutInflater());
        PickerListView = (android.widget.ListView)findViewById(R.id.item_list);
        PickerListView.setAdapter(PickerList);
        PickerList.setNotifyOnChange(false);
        PickerList.clear();

        Resources res= getApplicationContext().getResources();
        int[] objs= new int[]{R.raw.default_obj, R.raw.ch_obj, R.raw.bg4_obj};
        int[] mtls= new int[]{R.raw.default_mtl, R.raw.ch_mtl, R.raw.bg4_mtl};

        for (int i = 0; i < 3; i++) {
          PickerList.add(new PickerItem(new int[]{objs[i], mtls[i]}, res.getResourceName(objs[i]).split("_")[0].split("/")[1]));
        }
        PickerList.notifyDataSetChanged();
        ((FloatingActionButton)findViewById(R.id.item_select)).setOnClickListener
          (
            new View.OnClickListener()
              {
                public void onClick
                  (
                    View TheView
                  )
                  {
                    PickerItem Selected = null;
                    for (int i = 0;;)
                      {
                        if (i == PickerList.getCount())
                            break;
                        final PickerItem ThisItem =
                            (PickerItem)PickerListView.getItemAtPosition(i);
                        if (ThisItem.Selected)
                          {
                            Selected = ThisItem;
                            break;
                          } /*if*/
                        ++i;
                      } /*for*/
                    if (Selected != null)
                      {
                        startActivity(new Intent(Picker.this, Simulate.class).putExtra("objects", Selected.resIDs).addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_SINGLE_TOP).setAction(ACTION_VIEW));
                        finish();
                      } /*if*/
                  } /*onClick*/
              } /*OnClickListener*/
          );
      } /*onCreate*/

  } /*ug.karuhanga.planimeter.Picker*/
