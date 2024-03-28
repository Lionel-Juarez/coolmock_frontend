package com.example.hamacav1.bottomMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;
import com.example.hamacav1.entidades.reports.NewReport;
import com.example.hamacav1.entidades.reports.Report;
import com.example.hamacav1.entidades.reports.ReportsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment {

    private RecyclerView reportsRecyclerView;
    private ReportsAdapter reportsAdapter;
    private List<Report> reportsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        reportsRecyclerView = view.findViewById(R.id.reportsRecyclerView);
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Aquí deberías cargar los reportes desde tu base de datos en reportsList
        reportsList = new ArrayList<>();
        // Suponiendo que tienes un método para obtener el userId y la fecha actual correctamente
        String userId = getUserId(); // Implementa este método
        String currentDateTimeString = getCurrentDateTime(); // Implementa este método

        // Agregar reportes a la lista (ejemplo)
        reportsList.add(new Report("Título 1", "Descripción breve", "Nuevo", "Comentario completo", currentDateTimeString, userId));
        reportsList.add(new Report("Título 1", "Descripción breve", "Nuevo", "Comentario completo", currentDateTimeString, userId));
        reportsList.add(new Report("Título 1", "Descripción breve", "Nuevo", "Comentario completo", currentDateTimeString, userId));
        reportsList.add(new Report("Título 1", "Descripción breve", "Nuevo", "Comentario completo", currentDateTimeString, userId));
        reportsList.add(new Report("Título 1", "Descripción breve", "Nuevo", "Comentario completo", currentDateTimeString, userId));

        reportsAdapter = new ReportsAdapter(reportsList, getContext());
        reportsRecyclerView.setAdapter(reportsAdapter);

        // Ejemplo para agregar un nuevo reporte, ajusta según tu lógica y UI
        view.findViewById(R.id.fab_add_report).setOnClickListener(v -> newReport());

        return view;
    }

    private void newReport() {
        Intent intent = new Intent(getActivity(), NewReport.class);
        startActivity(intent);
    }

    // Métodos para obtener userId y fecha actual
    // Implementa estos métodos según tu lógica de aplicación
    private String getUserId() {
        // Lógica para obtener el userId
        return "userId"; // Ejemplo, ajusta esto
    }

    private String getCurrentDateTime() {
        // Lógica para obtener la fecha y hora actual
        return "dd/MM/yyyy HH:mm"; // Ejemplo, ajusta esto
    }


}