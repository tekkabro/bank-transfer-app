package com.example.bankapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @NotBlank(message = "Name cannot be blank")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    @NotNull
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    @Column(precision = 14, scale = 2)
    private BigDecimal balance;
}