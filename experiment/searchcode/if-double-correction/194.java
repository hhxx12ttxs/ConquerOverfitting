/**
 * Copyright: (c) Benedict W. Hazel, 2011-2012
 * IEncounter: Interface implemented by classes processing counterpoise
 * correction calculations.
 */

package uk.co.bwhazel.jcounterpoise;

import java.io.IOException;

/**
 * Defines methods implemented by classes processing counterpoise correction
 * calculations.
 * @author Benedict Hazel
 */
public interface IEncounter {

     /**
     * Gets the calculation description.
     * @return Calculation description.
     */
    String getDescription();

    /**
     * Gets the energy of the dimer in Hartree atomic units.
     * @return Dimer energy.
     */
    double getDimer();

    /**
     * Gets the energy of monomer A in dimer basis in Hartree atomic units.
     * @return Monomer A energy in dimer basis.
     */
    double getMonomerADimerBasis();

    /**
     * Gets the energy of monomer B in dimer basis in Hartree atomic units.
     * @return Monomer B energy in dimer basis.
     */
    double getMonomerBDimerBasis();

    /**
     * Gets the energy of monomer A in monomer A basis in Hartree atomic units.
     * @return Monomer A energy in monomer A basis.
     */
    double getMonomerAMonomerBasis();

    /**
     * Gets the energy of monomer B in monomer B basis in Hartree atomic units.
     * @return Monomer B energy in monomer B basis.
     */
    double getMonomerBMonomerBasis();

    /**
     * Gets the interaction energy between monomers in Hartree atomic units.
     * @return Interaction energy in Hartree atomic units.
     */
    double getInteractionEnergyHartrees();

    /**
     * Gets the interaction energy between monomers in kJ/mol.
     * @return Interaction energy in kJ/mol.
     */
    double getInteractionEnergyKjmol();

    /**
     * Gets the binding constant between monomers.
     * @return Binding constant.
     */
    double getBindingConstant();

    /**
     * Gets the number of energy values obtained from the counterpoise
     * correction calculation.
     * @return Number of energy values.
     */
    int getEnergyCount();

    /**
     * Processes the calculation file and stores energy values.
     * @param filename Counterpoise correction calculation file.
     * @throws IllegalArgumentException Exception thrown if the file is not
     * a Gaussian counterpoise correction calculation.
     * @throws IOException Exception thrown if an error occurs while reading
     * the file.
     */
    void setEnergies(String filename)
            throws IllegalArgumentException, IOException;

    /**
     * Sets the interaction energy values and binding constant.
     */
    void setInteractionEnergies();
}

