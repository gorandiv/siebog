package dnars.utils.importers.nt

import dnars.graph.DNarsGraphFactory
import scala.io.Source
import dnars.base.StatementParser

object DNarsImporter {
	def main(args: Array[String]): Unit = {
		if (args.length != 2) {
			println("I need 2 arguments: InputFile TargetKBName")
			return
		}
		val input = args(0)
		val kbname = args(1)
		
		println(s"Reading from $input...")
		val cfg = Map[String, Any]("storage.batch-loading" -> "true")
		val graph = DNarsGraphFactory.create(kbname, cfg)
		try {
			graph.eventManager.paused = true
			var counter = 0
			Source
				.fromFile(input)
				.getLines
				.foreach { line => {
					val st = StatementParser(line)
					graph.statements.add(st)
					
					counter += 1
					if (counter % 512 == 0)
						println(s"Imported $counter statements...")
				} }
			println(s"Done. Total: $counter statements.")
		} finally {
			graph.shutdown()
		}
	}
}