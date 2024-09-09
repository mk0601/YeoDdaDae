package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreDatabase {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 데이터 추가
    // String collection : 콜렉션명, HashMap<String, Object> hm : 데이터의 속성과 값을 가진 HashMap
    public void insertData (String collection, HashMap<String, Object> hm, OnFirestoreDataLoadedListener listener) {
        db.collection(collection)
                .add(hm)
                .addOnSuccessListener(queryDocumentSnapshot -> {
                    Log.d(TAG, "데이터 추가 성공 with ID: " + queryDocumentSnapshot.getId());
                    listener.onDataLoaded(queryDocumentSnapshot.getId());
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 추가 중 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    // 중복검사 true : 중복없음, false : 중복임
    // String collection : 콜렉션명, String targetField : 속성명, Object targetValue : 찾는값
    public void duplicationCheck(String collection, String targetField, Object targetValue, OnFirestoreDataLoadedListener listener) {
        db.collection(collection)
                .whereEqualTo(targetField, targetValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        listener.onDataLoaded(false);
                        Log.d(TAG, "데이터 with " + targetField + " = " + targetValue + " 가 이미 존재함");
                    } else {
                        listener.onDataLoaded(true);
                        Log.d(TAG, "데이터 with " + targetField + " = " + targetValue + " 가 없음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "중복검사 중 오류", e);
                });
    }

    // 데이터 수정
    // String collection : 콜렉션, String targetField : 찾을속성, Object targetValue : 찾을값, String updateField : 변경할속성, Object updateValue : 변경할값
    public void updateData (String collection, String targetField, Object targetValue, String updateField, Object updateValue) {
        db.collection(collection)
                .whereEqualTo(targetField, targetValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Check if the document contains the updateField
                        if (document.contains(updateField)) {
                            // Update the specified field with the new value
                            document.getReference().update(updateField, updateValue)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "데이터 수정 성공");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "데이터 수정 오류", e);
                                    });
                        } else {
                            Log.d(TAG, "데이터에 " + updateField + " 필드가 존재하지 않음");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                });
    }

    // 데이터삭제
    // String collection : 콜렉션명, String targetField : 속성명, Object targetValue : 찾는값
    public void deleteData (String collection, String targetField, Object targetValue) {
        db.collection(collection)
                .whereEqualTo(targetField, targetValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection(collection)
                                .document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "데이터 삭제 성공");
                                })
                                .addOnFailureListener(e -> {
                                    Log.d(TAG, "데이터 삭제 오류", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                });
    }

    // 콜렉션의 모든 데이터의 targetField의 값을 orderField의 값 순서대로 검색
    // 사용법은 아래에
    // String collection : 콜렉션명, String targetField : 속성명, String orderField : 정렬속성명
    public void selectAll(String collection, String targetField, String orderField, OnFirestoreDataLoadedListener listener) {
        List<Object> resultList = new ArrayList<>();

        db.collection(collection)
                .orderBy(orderField)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Object fieldValue = document.get(targetField);
                        if (fieldValue != null) {
                            resultList.add(fieldValue);
                        }
                    }
                    if (queryDocumentSnapshots.size() > 0) {
                        Log.d(TAG, "데이터 검색 성공");
                    } else {
                        Log.d(TAG, "해당 데이터가 없음");
                    }

                    listener.onDataLoaded(resultList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    // selectAll 사용법 예시
    /*
    fd.selectAll("콜렉션", "속성1", "속성2", new OnDataLoadedListener() {
        @Override
        public void onDataLoaded(Object data) {
            List<Object> resultList = (List<Object>) data;
            //resultList로 할 작업 작성
        }

        @Override
        public void onDataLoadError(String errorMessage) {
            //오류 발생
        }
     });
     */

    // 콜렉션의 데이터 중 targetField 필드가 targetValue인 문서의 resultField 필드의 값을 리턴
    // 사용법은 아래에
    // String collection : 콜렉션명, String targetField : 찾을 속성명, Object targetValue : 찾는 값, String resultField : 반환할 필드명
    public void selectOne(String collection, String targetField, Object targetValue, String resultField, OnFirestoreDataLoadedListener listener) {
        db.collection(collection)
                .whereEqualTo(targetField, targetValue)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Object resultValue = document.get(resultField);
                        if (resultValue != null) {
                            Log.d(TAG, "데이터 검색 성공");
                            listener.onDataLoaded(resultValue);
                            return;
                        }
                        else {
                            Log.d(TAG, "결과 값이 null");
                            return;
                        }
                    }
                    Log.d(TAG, "해당 데이터가 없음");
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    // selectOne 사용법 예시
    /*
    fd.selectOne("test", "속성1", 1111, "속성2", new OnDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object resultValue) {
                        // resultValue로 할 작업 작성
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        // 오류 발생
                    }
     */

    public void searchDocumentId (String collection, String targetField1, Object targetValue1, String targetField2, Object targetValue2, OnFirestoreDataLoadedListener listener) {
        db.collection(collection)
                .whereEqualTo(targetField1, targetValue1)
                .whereEqualTo(targetField2, targetValue2)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() == 1) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        listener.onDataLoaded(documentId);
                    }
                    else {
                        Log.d(TAG, "데이터가 없거나 여러개임");
                        listener.onDataLoadError("데이터가 없거나 여러개임");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void register (String id, String email, String uid, OnFirestoreDataLoadedListener listener) {
        HashMap<String, Object> newAccount = new HashMap<>();
        newAccount.put("id", id);
        newAccount.put("email", email);
        newAccount.put("isAdmin", false);
        newAccount.put("ydPoint", 0);
        newAccount.put("registerTime", FieldValue.serverTimestamp());

        db.collection("account").document(uid).set(newAccount)
                .addOnSuccessListener(aVoid -> {
                    listener.onDataLoaded(true);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "회원 문서 추가 중 오류 발생: " + e.getMessage());
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void findEmailAndIsAdminById (String id, OnFirestoreDataLoadedListener listener) {
        db.collection("account")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() == 1) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String email = (String) document.get("email");
                        String isAdmin = Boolean.toString((boolean) document.get("isAdmin"));
                        String result[] = {email, isAdmin};

                        listener.onDataLoaded(result);
                    }
                    else if (queryDocumentSnapshots.size() == 0 || queryDocumentSnapshots == null) {
                        listener.onDataLoadError("계정이 없음");
                    }
                    else {
                        listener.onDataLoadError("계정이 여러개임");
                    }
                })
                .addOnFailureListener(e -> {

                });
    }

    public void loadYdPoint(String id, OnFirestoreDataLoadedListener listener) {
        db.collection("account")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        long ydPoint = (long) document.get("ydPoint");

                        Log.d(TAG, "보유 여따대포인트 불러오기 성공");
                        listener.onDataLoaded(ydPoint);
                    }
                    else {
                        listener.onDataLoadError("해당 문서가 없음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "로그인 중 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadYdPointHistory(String id, OnFirestoreDataLoadedListener listener) {
        ArrayList<HashMap<String, Object>> chargeArrayList = new ArrayList<>();
        ArrayList<HashMap<String, Object>> refundArrayList = new ArrayList<>();
        ArrayList<HashMap<String, Object>> spendArrayList = new ArrayList<>();
        ArrayList<HashMap<String, Object>> receiveArrayList = new ArrayList<>();

        db.collection("chargeYdPoint")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1) {
                        HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                        data.put("documentId", documentSnapshot.getId());
                        chargeArrayList.add(data);
                    }

                    db.collection("refundYdPoint")
                            .whereEqualTo("id", id)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots2) {
                                    HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                                    data.put("documentId", documentSnapshot.getId());
                                    refundArrayList.add(data);
                                }

                                db.collection("spendYdPointHistory")
                                        .whereEqualTo("id", id)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots3) {
                                                HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                                                data.put("documentId", documentSnapshot.getId());
                                                spendArrayList.add(data);
                                            }

                                            db.collection("receiveYdPoint")
                                                    .whereEqualTo("id", id)
                                                    .get()
                                                    .addOnSuccessListener(queryDocumentSnapshots4 -> {
                                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots4) {
                                                            HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                                                            data.put("documentId", documentSnapshot.getId());
                                                            receiveArrayList.add(data);
                                                        }

                                                        Log.d(TAG, "충전수 : " + chargeArrayList.size());
                                                        Log.d(TAG, "환급수 : " + refundArrayList.size());
                                                        Log.d(TAG, "소비수 : " + spendArrayList.size());
                                                        Log.d(TAG, "받음수 : " + receiveArrayList.size());

                                                        ArrayList<ArrayList<HashMap<String, Object>>> resultArrayList = new ArrayList<>();
                                                        resultArrayList.add(chargeArrayList);
                                                        resultArrayList.add(refundArrayList);
                                                        resultArrayList.add(spendArrayList);
                                                        resultArrayList.add(receiveArrayList);

                                                        listener.onDataLoaded(resultArrayList);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.d(TAG, "포인트 받음 기록 찾기 실패", e);
                                                        listener.onDataLoadError("포인트 받음 기록 찾기 실패");
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "포인트 소비 기록 찾기 실패", e);
                                            listener.onDataLoadError("포인트 소비 기록 찾기 실패");
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.d(TAG, "포인트 환급 기록 찾기 실패", e);
                                listener.onDataLoadError("포인트 환급 기록 찾기 실패");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "포인트 충전 기록 찾기 실패", e);
                    listener.onDataLoadError("포인트 충전 기록 찾기 실패");
                });
    }

    public void chargeYdPoint(String id, int chargedYdPoint, OnFirestoreDataLoadedListener listener) {
        db.collection("account")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Long ydPoint = document.getLong("ydPoint");

                        if (ydPoint != null) {
                            db.collection("account")
                                    .document(document.getId())
                                    .update("ydPoint", ydPoint + chargedYdPoint)
                                    .addOnSuccessListener(aVoid -> {
                                        HashMap<String, Object> hm = new HashMap<>();
                                        hm.put("id", id);
                                        hm.put("chargedYdPoint", chargedYdPoint);
                                        hm.put("upTime", FieldValue.serverTimestamp());
                                        insertData("chargeYdPoint", hm, new OnFirestoreDataLoadedListener() {
                                            @Override
                                            public void onDataLoaded(Object data) {
                                                Log.d(TAG, "포인트 충전 성공");
                                                listener.onDataLoaded(true);
                                            }

                                            @Override
                                            public void onDataLoadError(String errorMessage) {
                                                Log.d(TAG, errorMessage);
                                                listener.onDataLoadError("포인트 충전 문서 기록 중 오류 발생");
                                            }
                                        });

                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "포인트 충전 실패", e);
                                        listener.onDataLoadError("포인트 충전에 실패했습니다.");
                                    });
                        } else {
                            Log.d(TAG, "ydPoint 값이 null입니다");
                            listener.onDataLoadError("ydPoint 값이 null입니다.");
                        }
                    } else {
                        Log.d(TAG, "해당 ID의 계정 없음");
                        listener.onDataLoadError("해당 ID의 계정을 찾을 수 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void refundYdPoint(String id, int refundYdPoint, String bank, String accountNumber, OnFirestoreDataLoadedListener listener) {
        db.collection("account")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Long ydPoint = document.getLong("ydPoint");

                        if (ydPoint != null) {
                            if (ydPoint >= refundYdPoint) {
                                db.collection("account")
                                        .document(document.getId())
                                        .update("ydPoint", ydPoint - refundYdPoint)
                                        .addOnSuccessListener(aVoid -> {
                                            HashMap<String, Object> hm = new HashMap<>();
                                            hm.put("id", id);
                                            hm.put("refundedYdPoint", refundYdPoint);
                                            hm.put("bank", bank);
                                            hm.put("accountNumber", accountNumber);
                                            hm.put("upTime", FieldValue.serverTimestamp());
                                            insertData("refundYdPoint", hm, new OnFirestoreDataLoadedListener() {
                                                @Override
                                                public void onDataLoaded(Object data) {
                                                    Log.d(TAG, "포인트 환급 성공");
                                                    listener.onDataLoaded(true);
                                                }

                                                @Override
                                                public void onDataLoadError(String errorMessage) {
                                                    Log.d(TAG, errorMessage);
                                                    listener.onDataLoadError("포인트 충전 문서 기록 중 오류 발생");
                                                }
                                            });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "포인트 환급 중 오류", e);
                                            listener.onDataLoadError(e.getMessage());
                                        });

                            }
                            else {
                                Log.d(TAG, "환급 포인트가 보유 포인트보다 큽니다");
                                listener.onDataLoadError("환급 포인트가 보유 포인트보다 큽니다");
                            }
                        }
                        else {
                            Log.d(TAG, "ydPoint 값이 null입니다");
                            listener.onDataLoadError("ydPoint 값이 null입니다.");
                        }
                    }
                    else {
                        Log.d(TAG, "해당 ID의 계정 없음");
                        listener.onDataLoadError("해당 ID의 계정을 찾을 수 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void receiveYdPoint(String id, int price, String type, OnFirestoreDataLoadedListener listener) {
        // type : "환불"
        db.collection("account")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Long ydPoint = document.getLong("ydPoint");

                        if (ydPoint != null) {
                            db.collection("account")
                                    .document(document.getId())
                                    .update("ydPoint", ydPoint + price)
                                    .addOnSuccessListener(aVoid -> {
                                        HashMap<String, Object> hm = new HashMap<>();
                                        hm.put("id", id);
                                        hm.put("type", type);
                                        hm.put("receivedYdPoint", price);
                                        hm.put("upTime", FieldValue.serverTimestamp());
                                        insertData("receiveYdPoint", hm, new OnFirestoreDataLoadedListener() {
                                            @Override
                                            public void onDataLoaded(Object data) {
                                                Log.d(TAG, "포인트 받기 성공");
                                                listener.onDataLoaded(true);
                                            }

                                            @Override
                                            public void onDataLoadError(String errorMessage) {
                                                Log.d(TAG, errorMessage);
                                                listener.onDataLoadError("포인트 받기 문서 기록 중 오류 발생");
                                            }
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "포인트 받기 실패", e);
                                        listener.onDataLoadError("포인트 받기에 실패했습니다.");
                                    });
                        } else {
                            Log.d(TAG, "ydPoint 값이 null입니다");
                            listener.onDataLoadError("ydPoint 값이 null입니다.");
                        }
                    } else {
                        Log.d(TAG, "해당 ID의 계정 없음");
                        listener.onDataLoadError("해당 ID의 계정을 찾을 수 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void payByYdPoint(String id, int price, OnFirestoreDataLoadedListener listener) {
        db.collection("account")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Long ydPoint = document.getLong("ydPoint");

                        if (ydPoint != null && ydPoint >= price) {
                            db.collection("account")
                                    .document(document.getId())
                                    .update("ydPoint", ydPoint - price)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "포인트 차감 성공");
                                        listener.onDataLoaded(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "포인트 차감 실패", e);
                                        listener.onDataLoadError("포인트 차감에 실패했습니다.");
                                    });
                        } else {
                            Log.d(TAG, "포인트가 부족합니다");
                            listener.onDataLoadError("포인트가 부족합니다");
                        }
                    } else {
                        Log.d(TAG, "해당 ID의 계정 없음");
                        listener.onDataLoadError("해당 ID의 계정을 찾을 수 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void findDicountPark(double nowLat, double nowLon, double radiusKiloMeter, OnFirestoreDataLoadedListener listener) {
        ArrayList<ParkItem> resultList = new ArrayList<>();

        db.collection("reportDiscountPark")
                .whereEqualTo("isApproval", true)
                .whereEqualTo("isCancelled", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        double discountParkLat = (double) document.get("poiLat");
                        double discountParkLon = (double) document.get("poiLon");

                        TMapPolyLine tpolyline = new TMapPolyLine();
                        tpolyline.addLinePoint(new TMapPoint(nowLat, nowLon));
                        tpolyline.addLinePoint(new TMapPoint(discountParkLat, discountParkLon));
                        double distance = tpolyline.getDistance() / 1000; // km단위

                        if (distance <= radiusKiloMeter) {
                            resultList.add(new ParkItem(6, (String) document.get("parkName"), Double.toString(distance), null, (String) document.get("poiPhone"), (String) document.get("parkCondition"), (long) document.get("parkDiscount"), Double.toString(discountParkLat), Double.toString(discountParkLon), (String) document.get("poiID"), document.getId()));
                        }
                    }
                    if (resultList != null && resultList.size() != 0) {
                        Log.d(TAG, "제보주차장 검색 성공. 결과 수 : " + resultList.size());
                    }
                    else {
                        Log.d(TAG, "해당 제보주차장이 없음");
                    }
                    listener.onDataLoaded(resultList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }


    public void findSharePark(String id, double nowLat, double nowLon, double radiusKiloMeter, String startString, String endString, OnFirestoreDataLoadedListener listener) {
        ArrayList<ParkItem> resultList = new ArrayList<>();

        if (id == null || id.isEmpty()) {
            Log.d(TAG, "아이디가 null이거나 빈 문자열임");
            return;
        }

        db.collection("sharePark")
                .whereEqualTo("isApproval", true)
                .whereEqualTo("isCancelled", false)
                .whereNotEqualTo("ownerId", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Calendar ca = Calendar.getInstance();
                        int year = ca.get(Calendar.YEAR);
                        int month = ca.get(Calendar.MONTH) + 1;
                        int day = ca.get(Calendar.DAY_OF_MONTH);
                        int hour = ca.get(Calendar.HOUR_OF_DAY);
                        int minute = ca.get(Calendar.MINUTE);
                        String nowString = "";
                        nowString += year;
                        if (month < 10) {
                            nowString += "0";
                        }
                        nowString += month;
                        if (day < 10) {
                            nowString += "0";
                        }
                        nowString += day;
                        if (hour < 10) {
                            nowString += "0";
                        }
                        nowString += hour;
                        if (minute < 10) {
                            nowString += "0";
                        }
                        nowString += minute;

                        HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) document.get("time");
                        List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
                        Collections.sort(sortedKeys);
                        String endTime = sortedKeys.get(sortedKeys.size() - 1) + shareTime.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

                        if (nowString.compareTo(endTime) > 0) {
                            continue;
                        }

                        if (startString != null && endString != null) {
                            if (startString.compareTo(endString) >= 0) {
                                continue;
                            }

                            ArrayList<String> targetAL = new ArrayList<>();

                            for (String outer : sortedKeys) {
                                targetAL.add(outer + shareTime.get(outer).get(0));
                                targetAL.add(outer + shareTime.get(outer).get(1));
                            }

                            try {
                                if (!brrrr(startString, endString, arrrr(targetAL))) {
                                    continue;
                                }
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        double shareParkLat = (double) document.get("lat");
                        double shareParkLon = (double) document.get("lon");

                        TMapPolyLine tpolyline = new TMapPolyLine();
                        tpolyline.addLinePoint(new TMapPoint(nowLat, nowLon));
                        tpolyline.addLinePoint(new TMapPoint(shareParkLat, shareParkLon));
                        double distance = tpolyline.getDistance() / 1000; // km단위

                        if (distance <= radiusKiloMeter) {
                            String parkName = (String) document.get("ownerId") + ": " + (String) document.get("parkDetailAddress");
                            resultList.add(new ParkItem(3, parkName, Double.toString(distance), Long.toString((long) document.get("price")), (String) document.get("ownerPhone"), null, -1, Double.toString(shareParkLat), Double.toString(shareParkLon), null, document.getId()));
                        }
                    }
                    if (resultList != null && resultList.size() != 0) {
                        Log.d(TAG, "공유주차장 검색 성공. 결과 수 : " + resultList.size());
                    } else {
                        Log.d(TAG, "해당 공유주차장이 없음");
                    }
                    listener.onDataLoaded(resultList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    ArrayList<String> arrrr (ArrayList<String> al) throws ParseException {
        ArrayList<String> copyAl = al;
        Collections.sort(copyAl);

        outerLoop:
        while (true) {
            for (String item : copyAl) {
                if (item.endsWith("2400")) {
                    String cuttedString = item.substring(0, 8);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

                    Date date = sdf.parse(cuttedString);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.add(Calendar.DAY_OF_YEAR, 1);

                    Date nextDate = calendar.getTime();
                    String nextDateStr = sdf.format(nextDate);
                    String nextDay = nextDateStr + "0000";

                    if (copyAl.contains(nextDay)) {
                        copyAl.remove(item);
                        copyAl.remove(nextDay);

                        continue outerLoop;
                    }
                }
            }

            for (String logItem : copyAl) {
                Log.d(TAG, logItem);
            }

            return copyAl;
        }
    }

    boolean brrrr(String startString, String endSTring, ArrayList<String> al) {
        boolean result = false;
        int alSize = (al.size() / 2) - 1;

        for (int i = 0; i <= alSize; i++) {
            String alStart = al.get(i);
            String alEnd = al.get(i + 1);

            long startLong = Long.parseLong(startString);
            long endLong = Long.parseLong(endSTring);
            long startAlLong = Long.parseLong(alStart);
            long endAlLong = Long.parseLong(alEnd);

            if (startAlLong <= startLong && endLong <= endAlLong) {
                result = true;
                break;
            }
        }

        return result;
    }

    public void loadShareParkInfo (String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        HashMap<String, Object> hm = new HashMap<>();

        db.collection("sharePark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 문서가 존재할 경우 해당 문서의 데이터를 HashMap에 넣음
                        hm.putAll(documentSnapshot.getData());
                        listener.onDataLoaded(hm);
                    }
                    else {
                        listener.onDataLoadError("해당 문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void cancelSharePark (String id, String firestoreDocumentId, String cancelReason ,OnFirestoreDataLoadedListener listener) {
        db.collection("sharePark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if(documentSnapshot.get("ownerId").equals(id)) {
                            Calendar ca = Calendar.getInstance();
                            int year = ca.get(Calendar.YEAR);
                            int month = ca.get(Calendar.MONTH) + 1;
                            int day = ca.get(Calendar.DAY_OF_MONTH);
                            int hour = ca.get(Calendar.HOUR_OF_DAY);
                            int minute = ca.get(Calendar.MINUTE);
                            String nowString = "";
                            nowString += year;
                            if (month < 10) {
                                nowString += "0";
                            }
                            nowString += month;
                            if (day < 10) {
                                nowString += "0";
                            }
                            nowString += day;
                            if (hour < 10) {
                                nowString += "0";
                            }
                            nowString += hour;
                            if (minute < 10) {
                                nowString += "0";
                            }
                            nowString += minute;

                            HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                            List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
                            Collections.sort(sortedKeys);
                            String endTime = sortedKeys.get(sortedKeys.size() - 1) + shareTime.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

                            Log.d(TAG, "현재 시각 : " + nowString);
                            Log.d(TAG, "공유 마지막 날의 끝 시각 : " + endTime);
                            if (nowString.compareTo(endTime) > 0) {
                                Log.d(TAG, "공유 시간이 지나서 취소 불가");
                                listener.onDataLoadError("공유 시간이 지나서 취소 불가");
                            }
                            else {
                                db.collection("reservation")
                                        .whereNotEqualTo("isCancelled", false)
                                        .get()
                                        .addOnSuccessListener(documentSnapshots2 -> {
                                            if (documentSnapshots2.size() == 0) {
                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("isCancelled", true);
                                                updates.put("cancelReason", cancelReason);

                                                db.collection("sharePark")
                                                        .document(firestoreDocumentId)
                                                        .update(updates)
                                                        .addOnSuccessListener(aVoid -> {
                                                            listener.onDataLoaded(true);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.d(TAG, "공유 취소 실패");
                                                            listener.onDataLoadError(e.getMessage());
                                                        });
                                            }
                                            else {
                                                listener.onDataLoadError("예약이 있어서 취소 불가");
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "해당 공유주차장의 예약 찾기 실패");
                                            listener.onDataLoadError(e.getMessage());
                                        });
                            }
                        }
                        else {
                            listener.onDataLoadError("ID가 일치하지 않음");
                        }
                    }
                    else {
                        listener.onDataLoadError("문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    void calculateFreeSharePark(String id, OnFirestoreDataLoadedListener listener) {
        db.collection("sharePark")
                .whereEqualTo("ownerId", id)
                .whereEqualTo("isCalculated", false)
                .whereEqualTo("price", 0)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long targetCount[] = {0};
                    long didCount[] = {0};

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Calendar ca = Calendar.getInstance();
                        int year = ca.get(Calendar.YEAR);
                        int month = ca.get(Calendar.MONTH) + 1;
                        int day = ca.get(Calendar.DAY_OF_MONTH);
                        int hour = ca.get(Calendar.HOUR_OF_DAY);
                        int minute = ca.get(Calendar.MINUTE);
                        String nowString = "";
                        nowString += year;
                        if (month < 10) {
                            nowString += "0";
                        }
                        nowString += month;
                        if (day < 10) {
                            nowString += "0";
                        }
                        nowString += day;
                        if (hour < 10) {
                            nowString += "0";
                        }
                        nowString += hour;
                        if (minute < 10) {
                            nowString += "0";
                        }
                        nowString += minute;

                        HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                        List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
                        Collections.sort(sortedKeys);
                        String endTime = sortedKeys.get(sortedKeys.size() - 1) + shareTime.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

                        if (nowString.compareTo(endTime) > 0) {
                            targetCount[0]++;
                        }
                    }
                    if (targetCount[0] == 0) {
                        Log.d(TAG, "정산안된무료주차장없음");
                        listener.onDataLoaded(0);
                    }
                    else {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Calendar ca = Calendar.getInstance();
                            int year = ca.get(Calendar.YEAR);
                            int month = ca.get(Calendar.MONTH) + 1;
                            int day = ca.get(Calendar.DAY_OF_MONTH);
                            int hour = ca.get(Calendar.HOUR_OF_DAY);
                            int minute = ca.get(Calendar.MINUTE);
                            String nowString = "";
                            nowString += year;
                            if (month < 10) {
                                nowString += "0";
                            }
                            nowString += month;
                            if (day < 10) {
                                nowString += "0";
                            }
                            nowString += day;
                            if (hour < 10) {
                                nowString += "0";
                            }
                            nowString += hour;
                            if (minute < 10) {
                                nowString += "0";
                            }
                            nowString += minute;

                            HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                            List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
                            Collections.sort(sortedKeys);
                            String endTime = sortedKeys.get(sortedKeys.size() - 1) + shareTime.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

                            if (nowString.compareTo(endTime) > 0) {
                                db.collection("sharePark")
                                        .document(documentSnapshot.getId())
                                        .update("isCalculated", true)
                                        .addOnSuccessListener(aVoid -> {
                                            didCount[0]++;
                                            if (didCount[0] == targetCount[0]) {
                                                Log.d(TAG, "자동정산된무료공유주차장수 : " + targetCount[0]);
                                                listener.onDataLoaded(targetCount[0]);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "isCalculated true로 변경 실패");
                                            listener.onDataLoadError(e.getMessage());
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, e.getMessage());
                    listener.onDataLoadError(e.getMessage());
                });
    }

    void calculateShareParkPrice(String id, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        db.collection("sharePark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (id.equals((String) documentSnapshot.get("ownerId"))) {
                        if (!(boolean) documentSnapshot.get("isCalculated")) {
                            Calendar ca = Calendar.getInstance();
                            int year = ca.get(Calendar.YEAR);
                            int month = ca.get(Calendar.MONTH) + 1;
                            int day = ca.get(Calendar.DAY_OF_MONTH);
                            int hour = ca.get(Calendar.HOUR_OF_DAY);
                            int minute = ca.get(Calendar.MINUTE);
                            String nowString = "";
                            nowString += year;
                            if (month < 10) {
                                nowString += "0";
                            }
                            nowString += month;
                            if (day < 10) {
                                nowString += "0";
                            }
                            nowString += day;
                            if (hour < 10) {
                                nowString += "0";
                            }
                            nowString += hour;
                            if (minute < 10) {
                                nowString += "0";
                            }
                            nowString += minute;

                            HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                            List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
                            Collections.sort(sortedKeys);
                            String endTime = sortedKeys.get(sortedKeys.size() - 1) + shareTime.get(sortedKeys.get(sortedKeys.size() - 1)).get(1);

                            Log.d(TAG, "현재 시각 : " + nowString);
                            Log.d(TAG, "공유 마지막 날의 끝 시각 : " + endTime);
                            if (nowString.compareTo(endTime) > 0) {
                                db.collection("reservation")
                                        .whereEqualTo("shareParkDocumentName", firestoreDocumentId)
                                        .whereEqualTo("isCancelled", false)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            long totalPrice = 0;
                                            for (QueryDocumentSnapshot documentSnapshot2 : queryDocumentSnapshots) {
                                                totalPrice += (long) documentSnapshot2.get("price");
                                            }
                                            int calculatedValue = ((int) (totalPrice * 95 / 100));
                                            if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                                                Log.d(TAG, "정산할 예약 수 : " + queryDocumentSnapshots.size() );
                                            }
                                            else {
                                                Log.d(TAG, "정산할 예약이 없음");
                                            }
                                            receiveYdPoint(id, calculatedValue, "공유주차장 정산", new OnFirestoreDataLoadedListener() {
                                                @Override
                                                public void onDataLoaded(Object data) {
                                                    db.collection("sharePark")
                                                            .document(firestoreDocumentId)
                                                            .update("isCalculated", true)
                                                            .addOnSuccessListener(aVoid -> {
                                                                listener.onDataLoaded(true);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.d(TAG, "isCalculated true로 변경 실패");
                                                                listener.onDataLoadError(e.getMessage());
                                                            });
                                                }

                                                @Override
                                                public void onDataLoadError(String errorMessage) {

                                                }
                                            });
                                        })
                                        .addOnFailureListener(e -> {

                                        });
                            }
                            else {
                                listener.onDataLoadError("사용완료된 공유주차장이 아님");
                            }
                        }
                        else {
                            listener.onDataLoadError("이미 정산됨");
                        }
                    }
                    else {
                        listener.onDataLoadError("ID가 일치하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, e.getMessage());
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadAnotherReservations(String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        db.collection("reservation")
                .whereEqualTo("shareParkDocumentName", firestoreDocumentId)
                .whereEqualTo("isCancelled", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    HashMap<String,  HashMap<String, ArrayList<String>>> resultMap = new HashMap<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String documentId = documentSnapshot.getId();
                        HashMap<String, ArrayList<String>> times = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                        resultMap.put(documentId, times);
                    }
                    if (resultMap.size() > 0 && resultMap != null) {
                        Log.d (TAG, "다른 예약들 찾기 성공 갯수 : " + resultMap.size());
                    }
                    else {
                        Log.d(TAG, "다른 예약이 없음");
                    }
                    listener.onDataLoaded(resultMap);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, e.getMessage());
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadMyReservations (String loginId, OnFirestoreDataLoadedListener listener) {
        db.collection("reservation")
                .whereEqualTo("id", loginId)
                .orderBy("upTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HashMap<String, Object>> resultArrayList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                        data.put("documentId", documentSnapshot.getId());
                        resultArrayList.add(data);
                    }
                    if (resultArrayList.size() > 0 && resultArrayList != null) {
                        Log.d (TAG, "내 예약들 찾기 성공 갯수 : " + resultArrayList.size());
                    }
                    else {
                        Log.d(TAG, "내 예약이 없음");
                    }
                    listener.onDataLoaded(resultArrayList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadReservation (String loginId, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        HashMap<String, Object> hm = new HashMap<>();

        db.collection("reservation")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if(documentSnapshot.get("id").equals(loginId)) {
                            hm.putAll(documentSnapshot.getData());
                            listener.onDataLoaded(hm);
                        }
                        else {
                            listener.onDataLoadError("아이디가 일치하지 않음");
                        }
                    }
                    else {
                        listener.onDataLoadError("해당 문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void cancelReservation(String loginId, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        db.collection("reservation")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if(documentSnapshot.get("id").equals(loginId)) {
                            Calendar ca = Calendar.getInstance();
                            int year = ca.get(Calendar.YEAR);
                            int month = ca.get(Calendar.MONTH) + 1;
                            int day = ca.get(Calendar.DAY_OF_MONTH);
                            int hour = ca.get(Calendar.HOUR_OF_DAY);
                            int minute = ca.get(Calendar.MINUTE);
                            String nowString = "";
                            nowString += year;
                            if (month < 10) {
                                nowString += "0";
                            }
                            nowString += month;
                            if (day < 10) {
                                nowString += "0";
                            }
                            nowString += day;
                            if (hour < 10) {
                                nowString += "0";
                            }
                            nowString += hour;
                            if (minute < 10) {
                                nowString += "0";
                            }
                            nowString += minute;

                            HashMap<String, ArrayList<String>> reservationTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                            List<String> sortedKeys = new ArrayList<>(reservationTime.keySet());
                            Collections.sort(sortedKeys);
                            String firstTime = sortedKeys.get(0) + reservationTime.get(sortedKeys.get(0)).get(0);

                            Log.d(TAG, "현재 시각 : " + nowString);
                            Log.d(TAG, "예약 첫 날의 시작 시각 : " + firstTime);
                            if (nowString.compareTo(firstTime) > 0) {
                                Log.d(TAG, "예약 시간이 지나서 취소 불가");
                                listener.onDataLoadError("예약 시간이 지나서 취소 불가");
                                return;
                            }
                            int price = (int)((long) documentSnapshot.get("price"));

                            db.collection("reservation")
                                    .document(firestoreDocumentId)
                                    .update("isCancelled", true)
                                    .addOnSuccessListener(aVoid -> {
                                        receiveYdPoint(loginId, price, "환불", new OnFirestoreDataLoadedListener() {
                                            @Override
                                            public void onDataLoaded(Object data) {
                                                Log.d(TAG, "예약 취소 성공");
                                                listener.onDataLoaded(1);
                                            }

                                            @Override
                                            public void onDataLoadError(String errorMessage) {

                                            }
                                        });


                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "예약 취소 실패");
                                        listener.onDataLoadError(e.getMessage());
                                    });
                        }
                        else {
                            listener.onDataLoadError("ID가 일치하지 않음");
                        }
                    }
                    else {
                        listener.onDataLoadError("문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadMyReports (String loginId, OnFirestoreDataLoadedListener listener) {
        db.collection("reportDiscountPark")
                .whereEqualTo("reporterId", loginId)
                .orderBy("upTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HashMap<String, Object>> resultArrayList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                        data.put("documentId", documentSnapshot.getId());
                        resultArrayList.add(data);
                    }
                    if (resultArrayList.size() > 0 && resultArrayList != null) {
                        Log.d (TAG, "내 제보들 찾기 성공 갯수 : " + resultArrayList.size());
                    }
                    else {
                        Log.d(TAG, "내 제보가 없음");
                    }
                    listener.onDataLoaded(resultArrayList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadOneReport (String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        HashMap<String, Object> hm = new HashMap<>();

        db.collection("reportDiscountPark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        hm.putAll(documentSnapshot.getData());
                        listener.onDataLoaded(hm);
                    }
                    else {
                        listener.onDataLoadError("해당 문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadMyOneReport (String loginId, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        HashMap<String, Object> hm = new HashMap<>();

        db.collection("reportDiscountPark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if(documentSnapshot.get("reporterId").equals(loginId)) {
                            hm.putAll(documentSnapshot.getData());
                            listener.onDataLoaded(hm);
                        }
                        else {
                            listener.onDataLoadError("아이디가 일치하지 않음");
                        }
                    }
                    else {
                        listener.onDataLoadError("해당 문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void cancelReport (String loginId, String firestoreDocumentId, String cancelReason, OnFirestoreDataLoadedListener listener) {
        db.collection("reportDiscountPark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if(documentSnapshot.get("reporterId").equals(loginId)) {
                            if (!(boolean) documentSnapshot.get("isApproval")) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("isCancelled", true);
                                updates.put("cancelReason", cancelReason);

                                db.collection("reportDiscountPark")
                                        .document(firestoreDocumentId)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "제보 취소 성공");
                                            if (!cancelReason.equals("공유자가 취소")) {
                                                db.collection("rateReport")
                                                        .whereEqualTo("reportDocumentID", firestoreDocumentId)
                                                        .get()
                                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                                            if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() != 0) {
                                                                int docCount[] = {0};
                                                                for (DocumentSnapshot documentSnapshot2 : queryDocumentSnapshots) {
                                                                    if (documentSnapshot2.get("rate").equals("mistake") || documentSnapshot2.get("rate").equals("wrong")) {
                                                                        docCount[0]++;
                                                                    }
                                                                }

                                                                for (DocumentSnapshot documentSnapshot2 : queryDocumentSnapshots) {
                                                                    String raterId = (String) documentSnapshot2.get("id");

                                                                    int count[] = {1};
                                                                    receiveYdPoint(raterId, 200, "부정적 평가한 할인주차장 취소", new OnFirestoreDataLoadedListener() {
                                                                        @Override
                                                                        public void onDataLoaded(Object data) {
                                                                            if (docCount[0] == count[0]) {
                                                                                listener.onDataLoaded(true);
                                                                            }
                                                                            else {
                                                                                count[0]++;
                                                                            }
                                                                        }
                                                                        @Override
                                                                        public void onDataLoadError(String errorMessage) {
                                                                            Log.d(TAG, errorMessage);
                                                                            listener.onDataLoadError("포인트 지급 중 오류");
                                                                        }
                                                                    });

                                                                }
                                                            }
                                                            else {
                                                                listener.onDataLoaded(true);
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.d(TAG, "부정 평가자 찾기 중 오류 발생");
                                                            listener.onDataLoadError(e.getMessage());
                                                        });
                                            }
                                            else {
                                                listener.onDataLoaded(true);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "제보 취소 실패");
                                            listener.onDataLoadError(e.getMessage());
                                        });
                            }
                            else {
                                listener.onDataLoadError("승인된 제보임");
                            }
                        }
                        else {
                            listener.onDataLoadError("ID가 일치하지 않음");
                        }
                    }
                    else {
                        listener.onDataLoadError("문서가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void approveReport(String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        db.collection("reportDiscountPark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!(boolean) documentSnapshot.get("isApproval")) {
                        if (!(boolean) documentSnapshot.get("isCancelled")) {
                            String reporterId = (String) documentSnapshot.get("reporterId");

                            db.collection("reportDiscountPark")
                                    .document(firestoreDocumentId)
                                    .update("isApproval", true)
                                    .addOnSuccessListener(aVoid -> {
                                        receiveYdPoint(reporterId, 1000, "할인주차장 제보 승인", new OnFirestoreDataLoadedListener() {
                                            @Override
                                            public void onDataLoaded(Object data) {
                                                db.collection("rateReport")
                                                        .whereEqualTo("reportDocumentID", firestoreDocumentId)
                                                        .whereEqualTo("rate", "perfect")
                                                        .get()
                                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                                            int documentSize = queryDocumentSnapshots.size();
                                                            if (documentSize == 0) {
                                                                listener.onDataLoaded(true);
                                                            }
                                                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                                String raterId = (String) documentSnapshot.get("id");

                                                                int count[] = {1};
                                                                receiveYdPoint(raterId, 200, "긍정적 평가한 할인주차장 승인", new OnFirestoreDataLoadedListener() {
                                                                    @Override
                                                                    public void onDataLoaded(Object data) {
                                                                        if (documentSize == count[0]) {
                                                                            listener.onDataLoaded(true);
                                                                        }
                                                                        else {
                                                                            count[0]++;
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onDataLoadError(String errorMessage) {
                                                                        Log.d(TAG, errorMessage);
                                                                        listener.onDataLoadError(errorMessage);
                                                                    }
                                                                });
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.d(TAG, "평가자 포인트 지급 중 오류", e);
                                                            listener.onDataLoadError(e.getMessage());
                                                        });
                                            }

                                            @Override
                                            public void onDataLoadError(String errorMessage) {
                                                Log.d(TAG, errorMessage);
                                                listener.onDataLoadError(errorMessage);
                                            }
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "제보자 포인트 지급 중 오류", e);
                                        listener.onDataLoadError(e.getMessage());
                                    });
                        }
                        else{
                            listener.onDataLoadError("취소된 제보주차장임");
                        }
                    }
                    else {
                        listener.onDataLoadError("이미 승인된 제보주차장임");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadAnotherReports (int targetDistanceKM, double nowLat, double nowLon, String loginId, OnFirestoreDataLoadedListener listener) {
        db.collection("reportDiscountPark")
                .whereNotEqualTo("reporterId", loginId)
                .whereEqualTo("isApproval", false)
                .whereEqualTo("isCancelled", false)
                .orderBy("upTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HashMap<String, Object>> resultArrayList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                        if (targetDistanceKM == 0) {
                            HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                            data.put("documentId", documentSnapshot.getId());
                            resultArrayList.add(data);
                        }
                        else {
                            double reportLat = (double) documentSnapshot.get("poiLat");
                            double reportLon = (double) documentSnapshot.get("poiLon");

                            TMapPolyLine tpolyline = new TMapPolyLine();
                            tpolyline.addLinePoint(new TMapPoint(nowLat, nowLon));
                            tpolyline.addLinePoint(new TMapPoint(reportLat, reportLon));
                            double distance = tpolyline.getDistance() / 1000; // km단위

                            if (distance <= targetDistanceKM) {
                                HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                                data.put("documentId", documentSnapshot.getId());
                                resultArrayList.add(data);
                            }
                        }
                    }
                    if (resultArrayList.size() > 0 && resultArrayList != null) {
                        Log.d (TAG, "제보들 찾기 성공 갯수 : " + resultArrayList.size());
                    }
                    else {
                        Log.d(TAG, "제보가 없음");
                    }
                    listener.onDataLoaded(resultArrayList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadUnapprovedReports (OnFirestoreDataLoadedListener listener) {
        db.collection("reportDiscountPark")
                .whereEqualTo("isApproval", false)
                .whereEqualTo("isCancelled", false)
                .orderBy("upTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HashMap<String, Object>> resultArrayList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                        data.put("documentId", documentSnapshot.getId());
                        resultArrayList.add(data);
                    }

                    if (resultArrayList.size() > 0 && resultArrayList != null) {
                        Log.d (TAG, "승인 안된 할인주차장 제보 찾기 성공 갯수 : " + resultArrayList.size());
                    }
                    else {
                        Log.d(TAG, "승인 안된 할인주차장 제보가 없음");
                    }
                    listener.onDataLoaded(resultArrayList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadRateCount (String id, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        long[] perfectCount = {0};
        long[] mistakeCount = {0};
        long[] wrongCount = {0};
        long[] myRate = {0};

        db.collection("reportDiscountPark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    perfectCount[0] = (long) documentSnapshot.get("ratePerfectCount");
                    mistakeCount[0] = (long) documentSnapshot.get("rateMistakeCount");
                    wrongCount[0] = (long) documentSnapshot.get("rateWrongCount");

                    db.collection("rateReport")
                            .whereEqualTo("reportDocumentID", firestoreDocumentId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot documentSnapshot2 : queryDocumentSnapshots) {
                                    String rate = documentSnapshot2.getString("rate");
                                    if (rate.equals("perfect")) {
                                        if (((String) documentSnapshot2.get("id")).equals(id)) {
                                            myRate[0] = 1;
                                        }
                                    }
                                    else if (rate.equals("mistake")) {
                                        if (((String) documentSnapshot2.get("id")).equals(id)) {
                                            myRate[0] = 2;
                                        }
                                    }
                                    else if (rate.equals("wrong")) {
                                        if (((String) documentSnapshot2.get("id")).equals(id)) {
                                            myRate[0] = 3;
                                        }
                                    }
                                }

                                long [] result = {perfectCount[0], mistakeCount[0], wrongCount[0], myRate[0]};
                                listener.onDataLoaded(result);
                            })
                            .addOnFailureListener(e -> {
                                Log.d(TAG, "데이터 검색 오류", e);
                                listener.onDataLoadError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void updateRate (int clickedBtn, String loginId, String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        // clickedBtn은 0 : 완벽, 1 : 실수있음, 2 : 완전히 틀림

        String[] rate = {""};
        switch (clickedBtn) {
            case 0:
                rate[0] = "perfect";
                break;
            case 1:
                rate[0] = "mistake";
                break;
            case 2:
                rate[0] = "wrong";
                break;
        }

        db.collection("reportDiscountPark")
                .document(firestoreDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    long rateCount[] = {(long) documentSnapshot.get("ratePerfectCount"), (long) documentSnapshot.get("rateMistakeCount"), (long) documentSnapshot.get("rateWrongCount")};

                    // 먼저 해당 조건에 맞는 문서가 있는지 확인
                    db.collection("rateReport")
                            .whereEqualTo("id", loginId)
                            .whereEqualTo("reportDocumentID", firestoreDocumentId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (QueryDocumentSnapshot documentSnapshot2 : queryDocumentSnapshots) {
                                        // 문서 삭제
                                        if (documentSnapshot2.get("rate").equals(rate[0])) {
                                            documentSnapshot2.getReference().delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        if (rate[0].equals("perfect")) {
                                                            db.collection("reportDiscountPark")
                                                                    .document(firestoreDocumentId)
                                                                    .update("ratePerfectCount", rateCount[0] - 1)
                                                                    .addOnSuccessListener(aVoid2 -> {
                                                                        listener.onDataLoaded("평가 삭제됨 : " + rate[0]);
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                        listener.onDataLoadError(e.getMessage());
                                                                    });
                                                        }
                                                        else if (rate[0].equals("mistake")) {
                                                            db.collection("reportDiscountPark")
                                                                    .document(firestoreDocumentId)
                                                                    .update("rateMistakeCount", rateCount[1] - 1)
                                                                    .addOnSuccessListener(aVoid2 -> {
                                                                        listener.onDataLoaded("평가 삭제됨 : " + rate[0]);
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                        listener.onDataLoadError(e.getMessage());
                                                                    });
                                                        }
                                                        else if (rate[0].equals("wrong")) {
                                                            db.collection("reportDiscountPark")
                                                                    .document(firestoreDocumentId)
                                                                    .update("rateWrongCount", rateCount[2] - 1)
                                                                    .addOnSuccessListener(aVoid2 -> {
                                                                        listener.onDataLoaded("평가 삭제됨 : " + rate[0]);
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                        listener.onDataLoadError(e.getMessage());
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.d(TAG, "문서 삭제 오류", e);
                                                        listener.onDataLoadError(e.getMessage());
                                                    });
                                        }
                                        // 문서 rate 수정
                                        else {
                                            String originalRate = (String) documentSnapshot2.get("rate");

                                            documentSnapshot2.getReference().update("rate", rate[0])
                                                    .addOnSuccessListener(aVoid -> {
                                                        if (originalRate.equals("perfect")) {
                                                            db.collection("reportDiscountPark")
                                                                    .document(firestoreDocumentId)
                                                                    .update("ratePerfectCount", rateCount[0] - 1)
                                                                    .addOnSuccessListener(aVoid2 -> {

                                                                        if (rate[0].equals("mistake")) {
                                                                            db.collection("reportDiscountPark")
                                                                                    .document(firestoreDocumentId)
                                                                                    .update("rateMistakeCount", rateCount[1] + 1)
                                                                                    .addOnSuccessListener(aVoid3 -> {
                                                                                        listener.onDataLoaded(null);
                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                                        listener.onDataLoadError(e.getMessage());
                                                                                    });
                                                                        }
                                                                        else if (rate[0].equals("wrong")) {
                                                                            db.collection("reportDiscountPark")
                                                                                    .document(firestoreDocumentId)
                                                                                    .update("rateWrongCount", rateCount[2] + 1)
                                                                                    .addOnSuccessListener(aVoid3 -> {
                                                                                        listener.onDataLoaded(null);
                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                                        listener.onDataLoadError(e.getMessage());
                                                                                    });
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                        listener.onDataLoadError(e.getMessage());
                                                                    });
                                                        }
                                                        else if (originalRate.equals("mistake")) {
                                                            db.collection("reportDiscountPark")
                                                                    .document(firestoreDocumentId)
                                                                    .update("rateMistakeCount", rateCount[1] - 1)
                                                                    .addOnSuccessListener(aVoid2 -> {

                                                                        if (rate[0].equals("perfect")) {
                                                                            db.collection("reportDiscountPark")
                                                                                    .document(firestoreDocumentId)
                                                                                    .update("ratePerfectCount", rateCount[0] + 1)
                                                                                    .addOnSuccessListener(aVoid3 -> {
                                                                                        listener.onDataLoaded(null);
                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                                        listener.onDataLoadError(e.getMessage());
                                                                                    });
                                                                        }
                                                                        else if (rate[0].equals("wrong")) {
                                                                            db.collection("reportDiscountPark")
                                                                                    .document(firestoreDocumentId)
                                                                                    .update("rateWrongCount", rateCount[2] + 1)
                                                                                    .addOnSuccessListener(aVoid3 -> {
                                                                                        listener.onDataLoaded(null);
                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                                        listener.onDataLoadError(e.getMessage());
                                                                                    });
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                        listener.onDataLoadError(e.getMessage());
                                                                    });
                                                        }
                                                        else if (originalRate.equals("wrong")) {
                                                            db.collection("reportDiscountPark")
                                                                    .document(firestoreDocumentId)
                                                                    .update("rateWrongCount", rateCount[2] - 1)
                                                                    .addOnSuccessListener(aVoid2 -> {

                                                                        if (rate[0].equals("perfect")) {
                                                                            db.collection("reportDiscountPark")
                                                                                    .document(firestoreDocumentId)
                                                                                    .update("ratePerfectCount", rateCount[0] + 1)
                                                                                    .addOnSuccessListener(aVoid3 -> {
                                                                                        listener.onDataLoaded(null);
                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                                        listener.onDataLoadError(e.getMessage());
                                                                                    });
                                                                        }
                                                                        else if (rate[0].equals("mistake")) {
                                                                            db.collection("reportDiscountPark")
                                                                                    .document(firestoreDocumentId)
                                                                                    .update("rateMistakeCount", rateCount[1] + 1)
                                                                                    .addOnSuccessListener(aVoid3 -> {
                                                                                        listener.onDataLoaded(null);
                                                                                    })
                                                                                    .addOnFailureListener(e -> {
                                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                                        listener.onDataLoadError(e.getMessage());
                                                                                    });
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.d(TAG, "데이터 검색 오류", e);
                                                                        listener.onDataLoadError(e.getMessage());
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.d(TAG, "문서 업데이트 오류", e);
                                                        listener.onDataLoadError(e.getMessage());
                                                    });
                                        }
                                    }
                                }
                                else {
                                    // 문서가 존재하지 않으면 새로 추가
                                    HashMap<String, Object> data = new HashMap<>();
                                    data.put("id", loginId);
                                    data.put("reportDocumentID", firestoreDocumentId);
                                    data.put("rate", rate[0]);

                                    insertData("rateReport", data, new OnFirestoreDataLoadedListener() {
                                        @Override
                                        public void onDataLoaded(Object data) {

                                            if (rate[0].equals("perfect")) {
                                                db.collection("reportDiscountPark")
                                                        .document(firestoreDocumentId)
                                                        .update("ratePerfectCount", rateCount[0] + 1)
                                                        .addOnSuccessListener(aVoid -> {
                                                            listener.onDataLoaded("새로 평가함 : " + rate[0]);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.d(TAG, "데이터 검색 오류", e);
                                                            listener.onDataLoadError(e.getMessage());
                                                        });
                                            }
                                            else if (rate[0].equals("mistake")) {
                                                db.collection("reportDiscountPark")
                                                        .document(firestoreDocumentId)
                                                        .update("rateMistakeCount", rateCount[1] + 1)
                                                        .addOnSuccessListener(aVoid -> {
                                                            listener.onDataLoaded("새로 평가함 : " + rate[0]);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.d(TAG, "데이터 검색 오류", e);
                                                            listener.onDataLoadError(e.getMessage());
                                                        });
                                            }
                                            else if (rate[0].equals("wrong")) {
                                                db.collection("reportDiscountPark")
                                                        .document(firestoreDocumentId)
                                                        .update("rateWrongCount", rateCount[2] + 1)
                                                        .addOnSuccessListener(aVoid -> {
                                                            listener.onDataLoaded("새로 평가함 : " + rate[0]);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.d(TAG, "데이터 검색 오류", e);
                                                            listener.onDataLoadError(e.getMessage());
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onDataLoadError(String errorMessage) {
                                            Log.d(TAG, errorMessage);
                                            listener.onDataLoadError("주차장 제보 평가 중 오류 발생");
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.d(TAG, "데이터 검색 오류", e);
                                listener.onDataLoadError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadUnapprovedShareParks(OnFirestoreDataLoadedListener listener) {
        db.collection("sharePark")
                .whereEqualTo("isApproval", false)
                .whereEqualTo("isCancelled", false)
                .orderBy("upTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HashMap<String, Object>> resultArrayList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Calendar ca = Calendar.getInstance();
                        int year = ca.get(Calendar.YEAR);
                        int month = ca.get(Calendar.MONTH) + 1;
                        int day = ca.get(Calendar.DAY_OF_MONTH);
                        int hour = ca.get(Calendar.HOUR_OF_DAY);
                        int minute = ca.get(Calendar.MINUTE);
                        String nowString = "";
                        nowString += year;
                        if (month < 10) {
                            nowString += "0";
                        }
                        nowString += month;
                        if (day < 10) {
                            nowString += "0";
                        }
                        nowString += day;
                        if (hour < 10) {
                            nowString += "0";
                        }
                        nowString += hour;
                        if (minute < 10) {
                            nowString += "0";
                        }
                        nowString += minute;

                        HashMap<String, ArrayList<String>> shareTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                        List<String> sortedKeys = new ArrayList<>(shareTime.keySet());
                        Collections.sort(sortedKeys);
                        String firstTime = sortedKeys.get(0) + shareTime.get(sortedKeys.get(0)).get(0);

                        Log.d(TAG, "현재 시각 : " + nowString);
                        Log.d(TAG, "예약 첫 날의 시작 시각 : " + firstTime);
                        if (nowString.compareTo(firstTime) > 0) {
                            continue;
                        }

                        HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                        data.put("documentId", documentSnapshot.getId());
                        resultArrayList.add(data);
                    }
                    
                    if (resultArrayList.size() > 0 && resultArrayList != null) {
                        Log.d (TAG, "승인 안된 공유 주차장 찾기 성공 갯수 : " + resultArrayList.size());
                    }
                    else {
                        Log.d(TAG, "승인 안된 공유 주차장이 없음");
                    }
                    listener.onDataLoaded(resultArrayList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void loadMyShareParks(String id, OnFirestoreDataLoadedListener listener) {
        db.collection("sharePark")
                .whereEqualTo("ownerId", id)
                .orderBy("upTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HashMap<String, Object>> resultArrayList = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        HashMap<String, Object> data = new HashMap<>(documentSnapshot.getData());
                        data.put("documentId", documentSnapshot.getId());
                        resultArrayList.add(data);
                    }

                    if (resultArrayList.size() > 0 && resultArrayList != null) {
                        Log.d (TAG, "내 공유 주차장 찾기 성공 갯수 : " + resultArrayList.size());
                    }
                    else {
                        Log.d(TAG, "내 공유 주차장이 없음");
                    }
                    listener.onDataLoaded(resultArrayList);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "데이터 검색 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    public void approveSharePark(String firestoreDocumentId, OnFirestoreDataLoadedListener listener) {
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot documentSnapshot = transaction.get(db.collection("sharePark").document(firestoreDocumentId));

            if (!(boolean) documentSnapshot.get("isApproval")) {
                if (!(boolean) documentSnapshot.get("isCancelled")) {
                    Calendar ca = Calendar.getInstance();
                    int year = ca.get(Calendar.YEAR);
                    int month = ca.get(Calendar.MONTH) + 1;
                    int day = ca.get(Calendar.DAY_OF_MONTH);
                    int hour = ca.get(Calendar.HOUR_OF_DAY);
                    int minute = ca.get(Calendar.MINUTE);
                    String nowString = "";
                    nowString += year;
                    if (month < 10) {
                        nowString += "0";
                    }
                    nowString += month;
                    if (day < 10) {
                        nowString += "0";
                    }
                    nowString += day;
                    if (hour < 10) {
                        nowString += "0";
                    }
                    nowString += hour;
                    if (minute < 10) {
                        nowString += "0";
                    }
                    nowString += minute;

                    HashMap<String, ArrayList<String>> reservationTime = (HashMap<String, ArrayList<String>>) documentSnapshot.get("time");
                    List<String> sortedKeys = new ArrayList<>(reservationTime.keySet());
                    Collections.sort(sortedKeys);
                    String firstTime = sortedKeys.get(0) + reservationTime.get(sortedKeys.get(0)).get(0);

                    Log.d(TAG, "현재 시각 : " + nowString);
                    Log.d(TAG, "예약 첫 날의 시작 시각 : " + firstTime);
                    if (nowString.compareTo(firstTime) < 0) {
                        transaction.update(db.collection("sharePark").document(firestoreDocumentId), "isApproval", true);
                    } else {
                        throw new FirebaseFirestoreException("공유시간이 지남", FirebaseFirestoreException.Code.ABORTED);
                    }
                } else {
                    throw new FirebaseFirestoreException("취소된 공유주차장임", FirebaseFirestoreException.Code.ABORTED);
                }
            } else {
                throw new FirebaseFirestoreException("이미 승인된 공유주차장임", FirebaseFirestoreException.Code.ABORTED);
            }

            return null;
        }).addOnSuccessListener(aVoid -> {
            listener.onDataLoaded(true);
        }).addOnFailureListener(e -> {
            listener.onDataLoadError(e.getMessage());
        });
    }

    public void getStatistics (Timestamp startTime, Timestamp endTime, OnFirestoreDataLoadedListener listener) {
        HashMap<String, Long> resultHM = new HashMap<String, Long>();

        db.collection("account")
                .whereGreaterThanOrEqualTo("registerTime", startTime)
                .whereLessThanOrEqualTo("registerTime", endTime)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.size() == 0) {
                        resultHM.put("회원", 0L);
                    }
                    else {
                        resultHM.put("회원", (long) queryDocumentSnapshots.size());
                    }
                    db.collection("chargeYdPoint")
                            .whereGreaterThanOrEqualTo("upTime", startTime)
                            .whereLessThanOrEqualTo("upTime", endTime)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                long totalChargePoint = 0;

                                for (DocumentSnapshot document : queryDocumentSnapshots2) {
                                    totalChargePoint += (long) document.get("chargedYdPoint");
                                }
                                resultHM.put("충전총포인트", totalChargePoint);

                                if (totalChargePoint == 0) {
                                    resultHM.put("충전수", 0L);
                                }
                                else {
                                    resultHM.put("충전수", (long) queryDocumentSnapshots2.size());
                                }

                                db.collection("refundYdPoint")
                                        .whereGreaterThanOrEqualTo("upTime", startTime)
                                        .whereLessThanOrEqualTo("upTime", endTime)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                            long totalRefundPrice = 0;
                                            for (DocumentSnapshot document : queryDocumentSnapshots3) {
                                                totalRefundPrice += (long) document.get("refundedYdPoint");
                                            }
                                            resultHM.put("환급총액", totalRefundPrice);

                                            if (totalRefundPrice == 0) {
                                                resultHM.put("환급수", 0L);
                                            }
                                            else {
                                                resultHM.put("환급수", (long) queryDocumentSnapshots3.size());
                                            }
                                            db.collection("sharePark")
                                                    .whereGreaterThanOrEqualTo("upTime", startTime)
                                                    .whereLessThanOrEqualTo("upTime", endTime)
                                                    .get()
                                                    .addOnSuccessListener(queryDocumentSnapshots4 -> {
                                                        long canceledShareParkCount = 0;
                                                        long approvedShareParkCount = 0;
                                                        ArrayList<String> calculatedShareParkDocumentId = new ArrayList<>();

                                                        if (queryDocumentSnapshots4 == null || queryDocumentSnapshots4.size() == 0) {
                                                            resultHM.put("총공유주차장수", 0L);
                                                        }
                                                        else {
                                                            resultHM.put("총공유주차장수", (long) queryDocumentSnapshots4.size());

                                                            for (DocumentSnapshot document : queryDocumentSnapshots4) {
                                                                if ((boolean) document.get("isCancelled")) {
                                                                    canceledShareParkCount++;
                                                                }
                                                                else if ((boolean) document.get("isApproval")) {
                                                                    approvedShareParkCount++;

                                                                    if ((boolean) document.get("isCalculated")) {
                                                                        calculatedShareParkDocumentId.add(document.getId());
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        resultHM.put("취소공유주차장수", canceledShareParkCount);
                                                        resultHM.put("승인공유주차장수", approvedShareParkCount);

                                                        db.collection("reservation")
                                                                .whereGreaterThanOrEqualTo("upTime", startTime)
                                                                .whereLessThanOrEqualTo("upTime", endTime)
                                                                .get()
                                                                .addOnSuccessListener(queryDocumentSnapshots5 -> {
                                                                    long canceledReservationCount = 0;

                                                                    long totalReservationPay = 0;

                                                                    if (queryDocumentSnapshots5 == null || queryDocumentSnapshots5.size() == 0) {
                                                                        resultHM.put("총예약수", 0L);
                                                                    }
                                                                    else {
                                                                        resultHM.put("총예약수", (long) queryDocumentSnapshots5.size());

                                                                        for (DocumentSnapshot document : queryDocumentSnapshots5) {
                                                                            if ((boolean) document.get("isCancelled")) {
                                                                                canceledReservationCount++;
                                                                            }

                                                                            if (calculatedShareParkDocumentId.contains((String) document.get("shareParkDocumentName"))) {
                                                                                totalReservationPay += (long) document.get("price");
                                                                            }
                                                                        }
                                                                    }

                                                                    long totalCommission = totalReservationPay - ((int) totalReservationPay * 95 / 100);

                                                                    resultHM.put("취소예약수", canceledReservationCount);
                                                                    resultHM.put("총수수료", totalCommission);

                                                                    db.collection("reportDiscountPark")
                                                                            .whereGreaterThanOrEqualTo("upTime", startTime)
                                                                            .whereLessThanOrEqualTo("upTime", endTime)
                                                                            .get()
                                                                            .addOnSuccessListener(queryDocumentSnapshots6 -> {
                                                                                long canceledReportParkCount = 0;
                                                                                long approvedReportParkCount = 0;

                                                                                if (queryDocumentSnapshots6 == null || queryDocumentSnapshots6.size() == 0) {
                                                                                    resultHM.put("총제보주차장수", 0L);
                                                                                }
                                                                                else {
                                                                                    resultHM.put("총제보주차장수", (long) queryDocumentSnapshots6.size());

                                                                                    for (DocumentSnapshot document : queryDocumentSnapshots6) {
                                                                                        if ((boolean) document.get("isCancelled")) {
                                                                                            canceledReportParkCount++;
                                                                                        }
                                                                                        else if ((boolean) document.get("isApproval")) {
                                                                                            approvedReportParkCount++;
                                                                                        }
                                                                                    }
                                                                                }

                                                                                resultHM.put("취소제보주차장수", canceledReportParkCount);
                                                                                resultHM.put("승인제보주차장수", approvedReportParkCount);

                                                                                db.collection("receiveYdPoint")
                                                                                        .whereGreaterThanOrEqualTo("upTime", startTime)
                                                                                        .whereLessThanOrEqualTo("upTime", endTime)
                                                                                        .get()
                                                                                        .addOnSuccessListener(queryDocumentSnapshots7 -> {
                                                                                            long receivedApproveReportParkPoint = 0;
                                                                                            long receivedRatedReportParkPoint = 0;

                                                                                            if (queryDocumentSnapshots7 == null || queryDocumentSnapshots7.size() == 0) {
                                                                                                resultHM.put("총제보주차장승인지급포인트", 0L);
                                                                                                resultHM.put("총평가주차장승인지급포인트", 0L);
                                                                                            }
                                                                                            else {
                                                                                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots7) {
                                                                                                    if (((String) documentSnapshot.get("type")).equals("할인주차장 제보 승인")) {
                                                                                                        receivedApproveReportParkPoint += (long) documentSnapshot.get("receivedYdPoint");
                                                                                                    }
                                                                                                    else if (((String) documentSnapshot.get("type")).equals("긍정적 평가한 할인주차장 승인") || ((String) documentSnapshot.get("type")).equals("부정적 평가한 할인주차장 취소")) {
                                                                                                        receivedRatedReportParkPoint += (long) documentSnapshot.get("receivedYdPoint");
                                                                                                    }
                                                                                                }
                                                                                                resultHM.put("총제보주차장승인지급포인트", receivedApproveReportParkPoint);
                                                                                                resultHM.put("총평가주차장승인지급포인트", receivedRatedReportParkPoint);
                                                                                            }
                                                                                            listener.onDataLoaded(resultHM);
                                                                                        })
                                                                                        .addOnFailureListener(e -> {
                                                                                            Log.d(TAG, "포인트받기찾기 중 오류", e);
                                                                                            listener.onDataLoadError("포인트받기찾기 중 오류");
                                                                                        });
                                                                            })
                                                                            .addOnFailureListener(e -> {
                                                                                Log.d(TAG, "제보주차장찾기 중 오류", e);
                                                                                listener.onDataLoadError("제보주차장찾기 중 오류");
                                                                            });
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Log.d(TAG, "예약찾기 중 오류", e);
                                                                    listener.onDataLoadError("예약찾기 중 오류");
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.d(TAG, "공유주차장찾기 중 오류", e);
                                                        listener.onDataLoadError("공유주차장찾기 중 오류");
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "환급찾기 중 오류", e);
                                            listener.onDataLoadError("환급찾기 중 오류");
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.d(TAG, "충전찾기 중 오류", e);
                                listener.onDataLoadError("충전찾기 중 오류");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "회원찾기 중 오류", e);
                    listener.onDataLoadError("회원찾기 중 오류");
                });
    }

    public void loadAdminNotification(String id, OnFirestoreDataLoadedListener listener) {
        db.collection("account")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                    if ((boolean) queryDocumentSnapshots1.getDocuments().get(0).get("isAdmin")) {
                        int [] approveShareParkNotification = {0};
                        int [] approveReportNotification = {0};

                        db.collection("sharePark")
                                .whereEqualTo("isApproval", false)
                                .whereEqualTo("isCancelled", false)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                    approveShareParkNotification[0] = queryDocumentSnapshots2.size();

                                    db.collection("reportDiscountPark")
                                            .whereEqualTo("isApproval", false)
                                            .whereEqualTo("isCancelled", false)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                                approveReportNotification[0] = queryDocumentSnapshots3.size();

                                                listener.onDataLoaded(new int[] {approveShareParkNotification[0], approveReportNotification[0]});
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.d(TAG, "미승인 제보주차장 찾기 중 오류", e);
                                                listener.onDataLoadError(e.getMessage());
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.d(TAG, "미승인 공유주차장 찾기 중 오류", e);
                                    listener.onDataLoadError(e.getMessage());
                                });
                    }
                    else {
                        Log.d(TAG, "관리자가 아님");
                        listener.onDataLoadError("관리자가 아님");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "로그인 중 오류", e);
                    listener.onDataLoadError(e.getMessage());
                });
    }

    /*
    public void my (OnFirestoreDataLoadedListener listener) {
        Log.d (TAG, "my 호출됨");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d (TAG, "my 호출됨2");
                    StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B553881/Parking/PrkSttusInfo");
                    urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=SW%2Bzk19oOq3MYSQHFSd08425DV%2BqaKp%2F5%2B1ECPndYBDZeTwVuvDqm6iKLl5haFOJmpXQ3%2BhjVRHF3PL4eg7rug%3D%3D");
                    urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
                    urlBuilder.append("&" + URLEncoder.encode("format","UTF-8") + "=" + URLEncoder.encode("2", "UTF-8"));
                    URL url = new URL(urlBuilder.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-type", "application/json");
                    System.out.println("Response code: " + conn.getResponseCode());
                    BufferedReader rd;
                    if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    } else {
                        rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    }
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                    }
                    rd.close();
                    conn.disconnect();
                    Log.d(TAG, "my 호출됨3");

                    // JSON 문자열을 JSONObject로 파싱
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    // 원하는 데이터 추출
                    int totalCount = Integer.parseInt(jsonResponse.getString("totalCount"));

                    int numOfRows = 100; // 한 페이지 결과 수를 100으로 설정
                    int totalPages = (int) Math.ceil((double) totalCount / numOfRows); // 총 페이지 수 계산

                    for (int page = 1; page <= totalPages; page++) {
                        Log.d(TAG, "Page : " + page + "/" + totalPages + " 시작");
                        
                        // 각 페이지에 대한 URL 생성
                        StringBuilder pageUrlBuilder = new StringBuilder("http://apis.data.go.kr/B553881/Parking/PrkSttusInfo");
                        pageUrlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=SW%2Bzk19oOq3MYSQHFSd08425DV%2BqaKp%2F5%2B1ECPndYBDZeTwVuvDqm6iKLl5haFOJmpXQ3%2BhjVRHF3PL4eg7rug%3D%3D");
                        pageUrlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(page), "UTF-8"));
                        pageUrlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(numOfRows), "UTF-8"));
                        pageUrlBuilder.append("&" + URLEncoder.encode("format", "UTF-8") + "=" + URLEncoder.encode("2", "UTF-8"));

                        URL pageUrl = new URL(pageUrlBuilder.toString());
                        HttpURLConnection pageConn = (HttpURLConnection) pageUrl.openConnection();
                        pageConn.setRequestMethod("GET");
                        pageConn.setRequestProperty("Content-type", "application/json");
                        System.out.println("Response code: " + pageConn.getResponseCode());

                        BufferedReader pageRd;
                        if (pageConn.getResponseCode() >= 200 && pageConn.getResponseCode() <= 300) {
                            pageRd = new BufferedReader(new InputStreamReader(pageConn.getInputStream()));
                        } else {
                            pageRd = new BufferedReader(new InputStreamReader(pageConn.getErrorStream()));
                        }
                        StringBuilder pageResponse = new StringBuilder();
                        String pageLine;
                        while ((pageLine = pageRd.readLine()) != null) {
                            pageResponse.append(pageLine);
                        }
                        pageRd.close();
                        pageConn.disconnect();

                        JSONObject pageJsonResponse = new JSONObject(pageResponse.toString());
                        listener.onDataLoaded(pageJsonResponse.toString());

                        // "PrkSttusInfo" 키의 값을 JSONArray로 가져옴
                        JSONArray prkSttusInfoArray = pageJsonResponse.getJSONArray("PrkSttusInfo");

                        // JSONArray를 순회하며 각각의 객체에 대해 처리
                        for (int i = 0; i < prkSttusInfoArray.length(); i++) {
                            Log.d (TAG, (i + 1) + "/" + prkSttusInfoArray.length() + "데이터");
                            JSONObject prkInfo = prkSttusInfoArray.getJSONObject(i);

                            // 필요한 데이터 추출
                            String prkCmprtCo = prkInfo.getString("prk_cmprt_co");
                            String prkCenterId = prkInfo.getString("prk_center_id");
                            String prkPlceNm = prkInfo.getString("prk_plce_nm");
                            String prkPlceAdres = prkInfo.getString("prk_plce_adres");
                            String prkPlceEntrcLo = prkInfo.getString("prk_plce_entrc_lo");
                            String prkPlceEntrcLa = prkInfo.getString("prk_plce_entrc_la");

                            // 필요한 데이터를 HashMap으로 구성
                            HashMap<String, Object> hm = new HashMap<>();
                            hm.put("prk_cmprt_co", prkCmprtCo);
                            hm.put("prk_center_id", prkCenterId);
                            hm.put("prk_plce_nm", prkPlceNm);
                            hm.put("prk_plce_adres", prkPlceAdres);
                            hm.put("prk_plce_entrc_lo", prkPlceEntrcLo);
                            hm.put("prk_plce_entrc_la", prkPlceEntrcLa);

                            // insertData 메서드를 호출하여 Firestore에 데이터 삽입
                            insertData("parkApi", hm, new OnFirestoreDataLoadedListener() {
                                @Override
                                public void onDataLoaded(Object data) {

                                }

                                @Override
                                public void onDataLoadError(String errorMessage) {

                                }
                            });
                        }
                    }
                }
                catch (Exception e) {
                    listener.onDataLoadError(String.valueOf(e));
                }
            }
        }).start();
    }

     */
}
