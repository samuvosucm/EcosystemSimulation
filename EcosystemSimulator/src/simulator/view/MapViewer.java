package simulator.view;

import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.State;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class MapViewer extends AbstractMapViewer {

	// Anchura/altura/ de la simulación -- se supone que siempre van a ser iguales
	// al tamaño del componente
	private int _width;
	private int _height;

	// Número de filas/columnas de la simulación
	private int _rows;
	private int _cols;

	// Anchura/altura de una región
	int _rwidth;
	int _rheight;

	// Mostramos sólo animales con este estado. Los posibles valores de _currState
	// son null, y los valores deAnimal.State.values(). Si es null mostramos todo.
	State _currState;

	// En estos atributos guardamos la lista de animales y el tiempo que hemos
	// recibido la última vez para dibujarlos.
	volatile private Collection<AnimalInfo> _objs;
	volatile private Double _time;

	// Una clase auxilar para almacenar información sobre una especie
	private static class SpeciesInfo {
		private Integer _count;
		private Color _color;

		SpeciesInfo(Color color) {
			_count = 0;
			_color = color;
		}
	}

	// Un mapa para la información sobre las especies
	Map<String, SpeciesInfo> _kindsInfo = new HashMap<>();

	// El font que usamos para dibujar texto
	private Font _font = new Font("Arial", Font.BOLD, 12);

	// Indica si mostramos el texto la ayuda o no
	private boolean _showHelp;

	public MapViewer() {
		initGUI();
	}

	private void initGUI() {

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyChar()) {
				case 'h':
					_showHelp = !_showHelp;
					repaint();
					break;
				case 's':
					change_state();
					repaint();
				default:
				}
			}

		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				requestFocus(); // Esto es necesario para capturar las teclas cuando el ratón está sobre este
								// componente.
			}
		});

		// Por defecto mostramos todos los animales
		_currState = null;

		// Por defecto mostramos el texto de ayuda
		_showHelp = true;
	}
	
	private void change_state() {
		
		if (_currState == null)
			_currState = State.values()[0];
		else if (_currState == State.values()[State.values().length - 1])
			_currState = null;
		else
			_currState = State.values()[_currState.ordinal() + 1];
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D gr = (Graphics2D) g;
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Cambiar el font para dibujar texto
		g.setFont(_font);

		// Dibujar fondo blanco
		gr.setBackground(Color.WHITE);
		gr.clearRect(0, 0, _width, _height);

		// Dibujar los animales, el tiempo, etc.
		if (_objs != null)
			drawObjects(gr, _objs, _time);

		// TODO Mostrar el texto de ayuda si _showHelp es true. El texto a mostrar es el
		// siguiente (en 2 líneas):
		if (_showHelp) {
			g.setColor(Color.RED);
			g.drawString("h: toggle help", 10, 20);
			g.drawString("s: show animals of a specific state", 10, 35);
		}
	}

	private boolean visible(AnimalInfo a) {
		return _currState == null || a.get_state() == _currState;
	}

	private void drawObjects(Graphics2D g, Collection<AnimalInfo> animals, Double time) {
		
		g.setColor(Color.LIGHT_GRAY);
		for (int i = 1; i < _cols; i++)
			g.drawLine(i * _rwidth, 0, i * _rheight, _height);
		for (int i = 1; i < _rows; i++)
			g.drawLine(0, i * _rheight, _width, i * _rheight);
			
		// Dibujar los animales
		for (AnimalInfo a : animals) {

			// Si no es visible saltamos la iteración
			if (!visible(a))
				continue;

			// La información sobre la especie de 'a'
			SpeciesInfo esp_info = _kindsInfo.get(a.get_genetic_code());

			// TODO Si esp_info es null, añade una entrada correspondiente al mapa. Para el
			// color usa ViewUtils.get_color(a.get_genetic_code())
			if (esp_info == null)
				_kindsInfo.put(a.get_genetic_code(), new SpeciesInfo(ViewUtils.get_color(a.get_genetic_code())));
			
			SpeciesInfo tag_info = _kindsInfo.get(a.get_genetic_code());

			// TODO Incrementar el contador de la especie (es decir el contador dentro de
			// tag_info)
			tag_info._count += 1;
			
			// TODO Dibijar el animal en la posicion correspondiente, usando el color
			// tag_info._color. Su tamaño tiene que ser relativo a su edad, por ejemplo
			// edad/2+2. Se puede dibujar usando fillRoundRect, fillRect o fillOval.
			g.setColor(tag_info._color);
			g.fillRoundRect((int) a.get_position().getX(), (int) a.get_position().getY(), (int) a.get_age() / 2 + 3, (int) a.get_age() / 2 + 3, 5, 5);

		}

		// TODO Dibujar la etiqueta del estado visible, sin no es null.
		if (_currState != null) {
			g.setColor(Color.BLUE);
			drawStringWithRect(g, 10, _height - 35, "State: " + _currState.toString());
		}
		// TODO Dibujar la etiqueta del tiempo. Para escribir solo 3 decimales puede
		// usar String.format("%.3f", time)
		g.setColor(Color.MAGENTA);
		drawStringWithRect(g, 10, _height - 55, "Time: " + String.format("%.3f", _time));
		// TODO Dibujar la información de todas la especies. Al final de cada iteración
		// poner el contador de la especie correspondiente a 0 (para resetear el cuento)
		int yPos = _height - 75;
		for (Entry<String, SpeciesInfo> e : _kindsInfo.entrySet()) {
			g.setColor(e.getValue()._color);
			drawStringWithRect(g, 10, yPos, e.getKey() + ": " + e.getValue()._count);
			e.getValue()._count = 0;
			yPos -= 20;
		}
	}

	// Un método que dibujar un texto con un rectángulo
	void drawStringWithRect(Graphics2D g, int x, int y, String s) {
		Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
		g.drawString(s, x, y);
		g.drawRect(x - 1, y - (int) rect.getHeight(), (int) rect.getWidth() + 1, (int) rect.getHeight() + 5);
	}

	@Override
	public void update(List<AnimalInfo> objs, Double time) {
		_objs = objs;
		_time = time;
		repaint();
	}

	@Override
	public void reset(double time, MapInfo map, List<AnimalInfo> animals) {
		_width = map.get_width();
		_height = map.get_height();
		_rwidth = map.get_region_width();
		_rheight = map.get_region_height();
		_rows = map.get_rows();
		_cols = map.get_cols();
		
		// Esto cambia el tamaño del componente, y así cambia el tamaño de la ventana
		// porque en MapWindow llamamos a pack() después de llamar a reset
		setPreferredSize(new Dimension(map.get_width(), map.get_height()));

		// Dibuja el estado
		update(animals, time);
	}

}
