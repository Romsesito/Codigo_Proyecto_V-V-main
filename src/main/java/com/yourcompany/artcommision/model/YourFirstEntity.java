package com.yourcompany.artcommision.model;

import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.Required;
import org.openxava.model.Identifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * This is an example of an entity.
 * 
 * Feel free feel to rename, modify or remove at your taste.
 */

@Entity @Getter @Setter
public class YourFirstEntity extends Identifiable {
	
	@Column(length=50) @Required
	String description;
	
	LocalDate date;
	
	BigDecimal amount;

}
