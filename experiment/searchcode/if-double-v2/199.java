package br.edu.ufcg.lsd.seghidro.extratoropendap.interpolador;

import java.io.IOException;

import org.apache.log4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.VariableEnhanced;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.unidata.geoloc.LatLonPointImpl;
import br.edu.ufcg.lsd.seghidro.extratoropendap.exceptions.ExtratorOpendapException;
import br.edu.ufcg.lsd.seghidro.extratoropendap.model.Coordenadas;
import br.edu.ufcg.lsd.seghidro.extratoropendap.model.DataSet;
import br.edu.ufcg.lsd.seghidro.extratoropendap.util.NetcdfAPIUtil;

/**
 * 
 * Interpolador Bilinear específico para datasets acessíveis via OpenDap. O
 * dataset sendo acessado deve obedecer à convenção CF-1.0 ou COARDS.
 * 
 * @author Edigley P. Fraga, edigley@lsd.ufcg.edu.br
 * @since 12/03/2009
 * 
 */
public class InterpoladorBilinearOPeNDAP implements InterpoladorIF {

	private static Logger logger = Logger
			.getLogger(InterpoladorBilinearOPeNDAP.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.edu.ufcg.lsd.seghidro.mudancasClimaticas.extrator.interpolador.
	 * InterpoladorIF
	 * #interpola(br.edu.ufcg.lsd.seghidro.mudancasClimaticas.extrator.DataSet,
	 * br.edu.ufcg.lsd.seghidro.mudancasClimaticas.extrator.Coordenadas)
	 */
	@Override
	public Double interpola(DataSet dataSet, String variavel,
			Coordenadas coordenadas) throws ExtratorOpendapException {
		try {

			// índice da coordenada de tempo
			int time = dataSet.getIndexFromTime(coordenadas.getTime());
			if (time == -1) {
				throw new ExtratorOpendapException(
						"DataSet de origem não possui dados para a data especificada.");
			}

			// recuperando o grid que contém a variável especificada
			GeoGrid grid;
			if (variavel != null && !variavel.trim().isEmpty()) {
				grid = dataSet.getGrid(variavel);
				if (grid == null) {
					throw new ExtratorOpendapException(
							"DataSet de origem não possui dados para a variável especificada:"
									+ variavel);
				}
			} else {
				grid = dataSet.getFirstGrid();
			}

			// recuperando os eixos coordenados
			VariableEnhanced variable = grid.getVariable();
			GridCoordSystem coordinateSystem = grid.getCoordinateSystem();
			CoordinateAxis longitudeVariable = coordinateSystem.getXHorizAxis();
			CoordinateAxis latitudeVariable = coordinateSystem.getYHorizAxis();
			CoordinateAxis1D pressureLevelVariable = coordinateSystem.getVerticalAxis();
			
			// recuperando o indice da coordenada de pressao atmosferica
			int pressureLevelIndex = 
				pressureLevelVariable.findCoordElement(coordenadas.getNivelPressaoAtmosferica());

			// localiza, na grade, os índices mais próximos dos valores de
			// latitude e longitude informados.
			int[] xy = coordinateSystem
					.findXYindexFromCoord(coordenadas.getLongitude(),
							coordenadas.getLatitude(), null);
			int longitudeGrid = xy[0];
			int latitudeGrid = xy[1];

			// recuperando os valores referenciados pelos índices anteriormente
			// encontrados
			int x1 = longitudeGrid;
			int y1 = latitudeGrid;
			double longitude = NetcdfAPIUtil.readSingleValueAsFloat(
					longitudeVariable, new int[] { longitudeGrid });
			double latitude = NetcdfAPIUtil.readSingleValueAsFloat(
					latitudeVariable, new int[] { latitudeGrid });

			// Determina o raio de abrangência para o valor de longitude
			// encontrado.
			double raioDeAbrangenciaLongitude = findRangeValueFromCoordinate(
					longitudeVariable, longitudeGrid);

			// Determina o raio de abrangência para o valor de latitude
			// encontrado.
			double raioDeAbrangenciaLatitude = findRangeValueFromCoordinate(
					latitudeVariable, latitudeGrid);

			// ajusta os índices dos pontos de grade vizinhos mais
			// representativos para os valores de coordenadas de interesse.
			if (longitude > coordenadas.getLongitude()) {
				x1 = x1 - 1;
				if (latitude > coordenadas.getLatitude()) {
					y1 = y1 - 1;
				}
			} else {
				if (latitude > coordenadas.getLatitude()) {
					y1 = y1 - 1;
				}
			}

			// converte o valor da longitude da faixa (1 a 360) para a faixa
			// convensional (-180 a 180)
			double p1x = LatLonPointImpl
					.lonNormal(NetcdfAPIUtil.readSingleValueAsFloat(
							longitudeVariable, new int[] { x1 }));
			double p1y = NetcdfAPIUtil.readSingleValueAsFloat(latitudeVariable,
					new int[] { y1 });

			int x2 = x1 + 1;
			int y2 = y1 + 1;

			// determina os pesos dos valores de longitude vizinhos.
			double xw1 = (coordenadas.getLongitude() - p1x)
					/ (2 * raioDeAbrangenciaLongitude);
			double xw2 = 1d - xw1;

			// determina os pesos dos valores de latitude vizinhos.
			double yw1 = (coordenadas.getLatitude() - p1y)
					/ (2 * raioDeAbrangenciaLatitude);
			double yw2 = 1d - yw1;

			// shape para a leitura de um único valor em uma variável de três
			// dimensões.
			int[] unicoElemento = NetcdfAPIUtil.getUniqueShapeForGrid(grid);

			// realiza a leitura do valor para cada ponto de grade vizinho.
			double f11;
			double f12;
			double f21;
			double f22;
			if (grid.getRank() == 3) {
				f11 = NetcdfAPIUtil.readAsFloat(variable, new int[] { time, y1,
						x1 }, unicoElemento);
				f12 = NetcdfAPIUtil.readAsFloat(variable, new int[] { time, y2,
						x1 }, unicoElemento);
				f21 = NetcdfAPIUtil.readAsFloat(variable, new int[] { time, y1,
						x2 }, unicoElemento);
				f22 = NetcdfAPIUtil.readAsFloat(variable, new int[] { time, y2,
						x2 }, unicoElemento);
			} else if (grid.getRank() == 4) {
				f11 = NetcdfAPIUtil.readAsFloat(variable, new int[] { time, 
						pressureLevelIndex, y1, x1 }, unicoElemento);
				f12 = NetcdfAPIUtil.readAsFloat(variable, new int[] { time, 
						pressureLevelIndex, y2, x1 }, unicoElemento);
				f21 = NetcdfAPIUtil.readAsFloat(variable, new int[] { time, 
						pressureLevelIndex, y1, x2 }, unicoElemento);
				f22 = NetcdfAPIUtil.readAsFloat(variable, new int[] { time, 
						pressureLevelIndex, y2, x2 }, unicoElemento);
			} else {
				logger.error("Não pôde efetuar a leitura do DataSet. Dimensão '"
						+ grid.getRank() + "' não esperada.");
				throw new ExtratorOpendapException(
						"Não pôde efetuar a leitura do DataSet. Dimensão '"
								+ grid.getRank() + "' não esperada.");
			}

			// aplica a fórmula de interpolação bilinear, considerando o valor e
			// o peso de cada ponto de grade vizinho
			return f11 * xw2 * yw2 + f21 * xw1 * yw2 + f12 * xw2 * yw1 + f22
					* xw1 * yw1;

		} catch (IOException e) {
			logger.error("Não pôde efetuar a leitura do DataSet.", e);
			throw new ExtratorOpendapException(
					"Não pôde efetuar a leitura do DataSet.");
		} catch (InvalidRangeException e) {
			logger.error("Algoritmo de extração inadequado para o DataSet.", e);
			throw new ExtratorOpendapException(
					"Algoritmo de extração inadequado para o DataSet.");
		} catch (RuntimeException e) {
			logger.error(
					"Não conseguiu efetuar a leitura do DataSet. Motivo: ", e);
			throw new ExtratorOpendapException(
					"Não conseguiu efetuar a leitura do DataSet. Motivo: "
							+ e.getMessage());
		}
	}

	/**
	 * Determina o raio de abrangência para o valor de uma data coordenada (
	 * <tt>coordinate</tt>).
	 * 
	 * @param coordinate
	 *            A coordenada.
	 * @param coordinateIndex
	 *            O índice da coordenada no grid.
	 * @return o raio de abrangência para o valor de uma data coordenada (
	 *         <tt>coordinate</tt>).
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	private double findRangeValueFromCoordinate(CoordinateAxis coordinate,
			int coordinateIndex) throws IOException, InvalidRangeException {
		Array long_bounds = coordinate.read(new int[] { coordinateIndex - 1 },
				new int[] { 2 });
		IndexIterator longBoundsIt = long_bounds.getIndexIterator();
		double v1 = bringValueAsDouble(longBoundsIt.next());
		double v2 = bringValueAsDouble(longBoundsIt.next());
		return Math.abs(v2 - v1) / 2.0;
	}

	/**
	 * intuitive enough.
	 * 
	 * @param value
	 *            The value to be converted.
	 * @return The value converted.
	 */
	private double bringValueAsDouble(Object value) {
		double retorno;
		try {
			retorno = (Float) value;
		} catch (Exception e) {
			// it's no pretty but works
			retorno = (Double) value;
		}
		return retorno;
	}

}

