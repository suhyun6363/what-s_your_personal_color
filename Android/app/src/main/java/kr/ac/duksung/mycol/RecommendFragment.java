package kr.ac.duksung.mycol;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.widget.Toast;
import com.google.android.material.tabs.TabLayout;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Typeface;
import java.util.HashSet;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.graphics.Color;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


public class RecommendFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private SharedViewModel sharedViewModel;
    private TextView scanResultTextView;
    private TabLayout tabLayout;

    private static final String TAG = "RecommendFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);

        scanResultTextView = view.findViewById(R.id.titleTextView);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.getScannedResult().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.d(TAG, "Scan result text: " + s);
                if (s.startsWith("N-")) {
                    s = s.substring(2);
                    scanResultTextView.setText("Neutral Tone");
                } else {
                    scanResultTextView.setText(s);
                }
                // 스캔 결과가 변경되면 탭에 맞는 데이터를 다시 불러옴
                loadProductsBasedOnTab();
            }
        });

        tabLayout = view.findViewById(R.id.tabLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        Button nextbutton = view.findViewById(R.id.nextbutton);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList, getContext());
        recyclerView.setAdapter(adapter);

        setupTabs(); // 탭 초기화

        nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TotalRecommendFragment로 이동하는 코드
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                // TotalRecommendFragment 생성
                TotalRecommendFragment totalRecommendFragment = new TotalRecommendFragment();
                // 해당 Fragment의 레이아웃 파일로부터 인스턴스를 생성하여 전달
                fragmentTransaction.replace(R.id.menu_frame_layout, totalRecommendFragment);
                fragmentTransaction.addToBackStack(null); // 이전 fragment로 돌아갈 수 있도록 back stack에 추가
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    // 탭 초기화
    private void setupTabs() {
        String[] tabTitles = {"촉촉립", "뽀송립", "아이", "촉촉블러셔", "뽀송블러셔"};

        for (String title : tabTitles) {
            TabLayout.Tab tab = tabLayout.newTab();
            View customTabView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab, null);
            TextView tabTextView = customTabView.findViewById(R.id.tabTextView);
            tabTextView.setText(title);
            tab.setCustomView(customTabView);
            tabLayout.addTab(tab);
        }

        // 기본 탭 설정 및 기본 데이터 로드
        TabLayout.Tab initialTab = tabLayout.getTabAt(0);
        if (initialTab != null) {
            initialTab.select();
            View customView = initialTab.getCustomView();
            if (customView != null) {
                TextView tabTextView = customView.findViewById(R.id.tabTextView);
                tabTextView.setTextColor(getResources().getColor(android.R.color.black)); // 초기 선택된 탭의 텍스트 색상을 블랙으로 변경
            }
        }

        // 탭 선택 리스너 등록
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 선택된 탭의 텍스트 색상을 변경
                View customView = tab.getCustomView();
                if (customView != null) {
                    TextView tabTextView = customView.findViewById(R.id.tabTextView);
                    tabTextView.setTextColor(getResources().getColor(android.R.color.black)); // 여기에서 텍스트 색상을 블랙으로 변경
                }
                recyclerView.scrollToPosition(0);

                loadProductsBasedOnTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 선택되지 않은 탭의 텍스트 색상을 기본 색상으로 변경
                View customView = tab.getCustomView();
                if (customView != null) {
                    TextView tabTextView = customView.findViewById(R.id.tabTextView);
                    tabTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }


    // 선택된 탭에 따라 상품을 불러오는 메서드
    private void loadProductsBasedOnTab() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String result = scanResultTextView.getText().toString();
        // "Neutral Tone"일 경우 "N-"을 제외한 원래 결과 사용
        if ("Neutral Tone".equals(result)) {
            result = sharedViewModel.getScannedResult().getValue().substring(2);
        }
        int position = tabLayout.getSelectedTabPosition();

        switch (position) {
            case 0:
                fetchProductsFromFirestoreMoist(db, "립메이크업", result);
                break;
            case 1:
                fetchProductsFromFirestoreSmooth(db, "립메이크업", result);
                break;
            case 2:
                fetchProductsFromFirestore(db, "아이메이크업", result);
                break;
            case 3:
                fetchProductsFromFirestoreMoistBase(db, "베이스메이크업", result);
                break;
            case 4:
                fetchProductsFromFirestoreSmoothBase(db, "베이스메이크업", result);
                break;
        }
    }


    // TotalRecommendFragment로 이동하는 메서드
    private void navigateToTotalRecommendFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        TotalRecommendFragment totalRecommendFragment = new TotalRecommendFragment();
        fragmentTransaction.replace(R.id.menu_frame_layout, totalRecommendFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void fetchProductsFromFirestoreMoist(FirebaseFirestore db, String category, String result) {
        db.collection("new_data")
                .whereEqualTo("category_list2", category)
                .whereGreaterThanOrEqualTo("average_rate", 4.7)
                .whereEqualTo("result", result)
                .whereEqualTo("moisturizing", 1)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            productList.clear();
                            HashSet<String> productNames = new HashSet<>();
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String imageUrl = document.getString("img");
                                String productName = document.getString("name");
                                String optionName = document.getString("option_name");
                                List<Double> optionRgbList = (List<Double>) document.get("option_rgb"); // option_rgb 필드를 배열로 가져옴
                                String number = document.getString("number");

                                // optionRgbList를 문자열로 변환
                                String optionRgb = null;
                                if (optionRgbList != null) {
                                    optionRgb = optionRgbList.toString(); // 배열을 문자열로 변환
                                }

                                Log.d(TAG, "Product Name: " + productName + ", Option Name: " + optionName + ", Option RGB: " + optionRgb);

                                // 중복 체크
                                if (imageUrl != null && productName != null && optionName != null && number != null && optionRgb != null && !productNames.contains(productName)) {
                                    Product product = new Product(productName, optionName, imageUrl, number, optionRgb);
                                    productList.add(product);
                                    productNames.add(productName);
                                    count++;
                                }
                                if (count >= 5) // 이미 3개의 상품을 가져왔으면 더 이상 반복할 필요 없음
                                    break;
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }



                });
    }

    private void fetchProductsFromFirestoreSmooth(FirebaseFirestore db, String category,  String result) {
        db.collection("new_data")
                .whereEqualTo("category_list2", category)
                .whereGreaterThanOrEqualTo("average_rate", 4.7)
                .whereEqualTo("result", result)
                .whereEqualTo("moisturizing", 0)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            productList.clear();
                            HashSet<String> productNames = new HashSet<>();
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String imageUrl = document.getString("img");
                                String productName = document.getString("name");
                                String optionName = document.getString("option_name");
                                List<Double> optionRgbList = (List<Double>) document.get("option_rgb"); // option_rgb 필드를 배열로 가져옴
                                String number = document.getString("number");

                                // optionRgbList를 문자열로 변환
                                String optionRgb = null;
                                if (optionRgbList != null) {
                                    optionRgb = optionRgbList.toString(); // 배열을 문자열로 변환
                                }

                                Log.d(TAG, "Product Name: " + productName + ", Option Name: " + optionName + ", Option RGB: " + optionRgb);

                                // 중복 체크
                                if (imageUrl != null && productName != null && optionName != null && number != null && optionRgb != null && !productNames.contains(productName)) {
                                    Product product = new Product(productName, optionName, imageUrl, number, optionRgb);
                                    productList.add(product);
                                    productNames.add(productName);
                                    count++;
                                }
                                if (count >= 5) // 이미 3개의 상품을 가져왔으면 더 이상 반복할 필요 없음
                                    break;
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }


                });
    }

    private void fetchProductsFromFirestore(FirebaseFirestore db, String category,  String result) {
        db.collection("new_data")
                .whereEqualTo("category_list2", category)
                .whereGreaterThanOrEqualTo("average_rate", 4.7)
                .whereEqualTo("result", result)
                .whereIn("moisturizing", new ArrayList<Integer>() {{
                    add(0);
                    add(1);
                }})
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            productList.clear();
                            HashSet<String> productNames = new HashSet<>();
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String imageUrl = document.getString("img");
                                String productName = document.getString("name");
                                String optionName = document.getString("option_name");
                                List<Double> optionRgbList = (List<Double>) document.get("option_rgb"); // option_rgb 필드를 배열로 가져옴
                                String number = document.getString("number");

                                // optionRgbList를 문자열로 변환
                                String optionRgb = null;
                                if (optionRgbList != null) {
                                    optionRgb = optionRgbList.toString(); // 배열을 문자열로 변환
                                }

                                Log.d(TAG, "Product Name: " + productName + ", Option Name: " + optionName + ", Option RGB: " + optionRgb);

                                // 중복 체크
                                if (imageUrl != null && productName != null && optionName != null && number != null && optionRgb != null && !productNames.contains(productName)) {
                                    Product product = new Product(productName, optionName, imageUrl, number, optionRgb);
                                    productList.add(product);
                                    productNames.add(productName);
                                    count++;
                                }
                                if (count >= 5) // 이미 3개의 상품을 가져왔으면 더 이상 반복할 필요 없음
                                    break;
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }

    private void fetchProductsFromFirestoreMoistBase(FirebaseFirestore db, String category,  String result) {
        db.collection("new_data")
                .whereEqualTo("category_list2", category)
                .whereGreaterThanOrEqualTo("average_rate", 4.5)
                .whereEqualTo("result", result)
                .whereEqualTo("moisturizing", 1)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            productList.clear();
                            HashSet<String> productNames = new HashSet<>();
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String imageUrl = document.getString("img");
                                String productName = document.getString("name");
                                String optionName = document.getString("option_name");
                                List<Double> optionRgbList = (List<Double>) document.get("option_rgb"); // option_rgb 필드를 배열로 가져옴
                                String number = document.getString("number");

                                // optionRgbList를 문자열로 변환
                                String optionRgb = null;
                                if (optionRgbList != null) {
                                    optionRgb = optionRgbList.toString(); // 배열을 문자열로 변환
                                }

                                Log.d(TAG, "Product Name: " + productName + ", Option Name: " + optionName + ", Option RGB: " + optionRgb);

                                // 중복 체크
                                if (imageUrl != null && productName != null && optionName != null && number != null && optionRgb != null && !productNames.contains(productName)) {
                                    Product product = new Product(productName, optionName, imageUrl, number, optionRgb);
                                    productList.add(product);
                                    productNames.add(productName);
                                    count++;
                                }
                                if (count >= 5) // 이미 3개의 상품을 가져왔으면 더 이상 반복할 필요 없음
                                    break;
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }


                });
    }

    private void fetchProductsFromFirestoreSmoothBase(FirebaseFirestore db, String category,  String result) {
        db.collection("new_data")
                .whereEqualTo("category_list2", category)
                .whereGreaterThanOrEqualTo("average_rate", 4.5)
                .whereEqualTo("result", result)
                .whereEqualTo("moisturizing", 0)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            productList.clear();
                            HashSet<String> productNames = new HashSet<>();
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String imageUrl = document.getString("img");
                                String productName = document.getString("name");
                                String optionName = document.getString("option_name");
                                List<Double> optionRgbList = (List<Double>) document.get("option_rgb"); // option_rgb 필드를 배열로 가져옴
                                String number = document.getString("number");

                                // optionRgbList를 문자열로 변환
                                String optionRgb = null;
                                if (optionRgbList != null) {
                                    optionRgb = optionRgbList.toString(); // 배열을 문자열로 변환
                                }

                                Log.d(TAG, "Product Name: " + productName + ", Option Name: " + optionName + ", Option RGB: " + optionRgb);

                                // 중복 체크
                                if (imageUrl != null && productName != null && optionName != null && number != null && optionRgb != null && !productNames.contains(productName)) {
                                    Product product = new Product(productName, optionName, imageUrl, number, optionRgb);
                                    productList.add(product);
                                    productNames.add(productName);
                                    count++;
                                }
                                if (count >= 5) // 이미 3개의 상품을 가져왔으면 더 이상 반복할 필요 없음
                                    break;
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }


                }

                });
    }
}
