class Stack<T> {
    private data class Node<T>(var data: T, var next: Node<T>? = null)

    private var head: Node<T>? = null
    private var size: Int = 0

    fun append(value : T) {
        if (head == null) {
            head = Node(value)
        } else {
            var newNode = Node(value, head)
            head = newNode
        }
        size++
    }

    fun pop() {
        if (head != null) {
            head = head?.next
            size--
        }
    }

    fun getCurrValue(): T? {
        return head?.data
    }

    fun isNotEmpty() : Boolean {
        return size != 0
    }
    fun clear() {
        head = null
        size = 0
    }

}
