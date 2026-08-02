export default function CartLine({ item, onRemove }) {
  return (
    <tr className="cart-line">
      <td>{item.productName}</td>
      <td>${Number(item.unitPrice).toFixed(2)}</td>
      <td>{item.quantity}</td>
      <td>${Number(item.lineTotal).toFixed(2)}</td>
      <td>
        <button type="button" className="btn btn-danger" onClick={() => onRemove(item.id)}>
          Remove
        </button>
      </td>
    </tr>
  )
}
