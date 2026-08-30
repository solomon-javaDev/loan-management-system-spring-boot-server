package io.sol.loanmanagementsystemspringbootserver.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class SystemSetting {

    @Id
    @Column(name = "setting_key")
    private String key;

    @Column(name = "setting_value")
    private String value;


}
