interface S{
	keycode: String,
	name: String,
	timeout: Number
}
interface INDEX {
	name: string
	hp: [number, number]
	mp: [number, number]
	position: { x: Number, y: number },
	positionInScreen: { x: number, y: number },
	skills: S,
	pets: []
}