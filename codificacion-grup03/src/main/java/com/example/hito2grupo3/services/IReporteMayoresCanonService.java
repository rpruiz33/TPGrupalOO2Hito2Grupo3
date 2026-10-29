package com.example.hito2grupo3.services;

import java.util.List;
import com.example.hito2grupo3.dto.ReporteMayoresCanonDTO;

public interface IReporteMayoresCanonService {

    List<ReporteMayoresCanonDTO> obtenerMayoresCanon(int cantidad);
}