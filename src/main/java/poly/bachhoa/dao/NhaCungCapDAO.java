/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package poly.bachhoa.dao;

import java.util.List;
import poly.bachhoa.entity.NhaCungCap;

/**
 *
 * @author vuong
 */
public interface NhaCungCapDAO {
    boolean insert(NhaCungCap ncc);
    boolean update(NhaCungCap ncc);
    boolean delete(String maNCC);
    NhaCungCap selectById(String maNCC);
    List<NhaCungCap> selectAll();
    List<NhaCungCap> selectByKeyword(String keyword);
    String getMaxMaNCC();
}
