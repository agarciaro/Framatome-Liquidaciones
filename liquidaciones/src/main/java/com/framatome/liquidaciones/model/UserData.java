package com.framatome.liquidaciones.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserData {
	
	private String userId;
	private String nombre;
	private String apellidos;
	
}
