package com.mellah.mediassist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class WelcomeSetupActivity extends AppCompatActivity {
    private ViewPager2 vp;
    private TabLayout dots;
    private Button btnSkip, btnNext;

    private SlideAdapter adapter;
    private List<SlideItem> slides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_setup);

        vp      = findViewById(R.id.vpOnboarding);
        dots    = findViewById(R.id.tabDots);
        btnSkip = findViewById(R.id.btnSkip);
        btnNext = findViewById(R.id.btnNext);

        // 1) Prepare your slide data
        slides = new ArrayList<>();
        slides.add(new SlideItem(
                R.drawable.medication,
                "Manage Medications",
                "Add, edit and get reminders for all your medications."
        ));
        slides.add(new SlideItem(
                R.drawable.appointment,
                "Track Appointments",
                "Never miss a doctor visit with easy scheduling."
        ));
        slides.add(new SlideItem(
                R.drawable.emergency,
                "Emergency Contacts",
                "Store and call your emergency contacts in one tap."
        ));
        slides.add(new SlideItem(
                R.drawable.aidoctor,
                "AI Doctor",
                "Your personal doctor powered using artificial intelligence."
        ));

        // …add more slides as needed…

        adapter = new SlideAdapter(slides);
        vp.setAdapter(adapter);

        // 2) Hook up dots indicator
        new TabLayoutMediator(dots, vp,
                (tab, position) -> { /* no label */ }
        ).attach();

        // 3) Button behaviors
        btnSkip.setOnClickListener(v -> finishOnboarding());
        btnNext.setOnClickListener(v -> {
            int next = vp.getCurrentItem() + 1;
            if (next < slides.size()) {
                vp.setCurrentItem(next);
            } else {
                finishOnboarding();
            }
        });

        // 4) Change "Next" text on last page
        vp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
            @Override
            public void onPageSelected(int pos) {
                btnNext.setText(pos == slides.size()-1 ? "Start" : "Next");
            }
        });
    }

    private void finishOnboarding() {
        // back to MainActivity which now will skip "first run"
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
