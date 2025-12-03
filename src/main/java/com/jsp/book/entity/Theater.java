package com.jsp.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

@Data
@Entity
public class Theater {
	@Id
	@GeneratedValue(generator = "tid")
	@SequenceGenerator(name = "tid", initialValue = 10001, allocationSize = 1)
	private Long id;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false, length = 500)
	private String address;
	@Column(nullable = false, length = 500)
	private String locationLink;
	@Column(nullable = false)
	private String imageLocation;
}
